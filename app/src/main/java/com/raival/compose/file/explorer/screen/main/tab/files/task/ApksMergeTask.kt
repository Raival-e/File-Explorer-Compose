package com.raival.compose.file.explorer.screen.main.tab.files.task

import com.android.apksig.ApkSigner
import com.android.apksig.KeyConfig
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.toFormattedDate
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.reandroid.apkeditor.merge.Merger
import com.reandroid.apkeditor.merge.MergerOptions
import java.io.File
import java.security.KeyFactory
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec

class ApksMergeTask(
    val sourceContent: ContentHolder
) : Task() {
    private var aborted = false
    private var parameters: ApksMergeTaskParameters? = null

    override val metadata = System.currentTimeMillis().toFormattedDate().let { time ->
        TaskMetadata(
            id = id,
            creationTime = time,
            title = globalClass.resources.getString(R.string.convert_to_apk),
            subtitle = globalClass.resources.getString(R.string.task_subtitle, sourceContent.size),
            displayDetails = sourceContent.displayName,
            fullDetails = buildString {
                append(sourceContent.displayName)
                append("\n")
                append(time)
            },
            isCancellable = false,
            canMoveToBackground = false
        )
    }

    override val progressMonitor = TaskProgressMonitor(
        status = TaskStatus.PENDING,
        taskTitle = metadata.title,
    )

    override fun getCurrentStatus() = progressMonitor.status

    override fun validate() = sourceContent.isValid()

    override fun abortTask() {
        aborted = true
    }

    private fun markAsFailed(info: String) {
        progressMonitor.apply {
            status = TaskStatus.FAILED
            summary = info
        }
    }

    override suspend fun run(params: TaskParameters) {
        parameters = params as ApksMergeTaskParameters
        progressMonitor.status = TaskStatus.RUNNING

        if (!sourceContent.isValid() || sourceContent !is LocalFileHolder) {
            markAsFailed(globalClass.resources.getString(R.string.invalid_bundle_file))
            return
        }

        progressMonitor.processName = globalClass.resources.getString(R.string.merging)

        val mergedFile = mergeBundleFile(sourceContent)

        // mergeBundleFile function will handle failed task
        if (mergedFile != null && parameters!!.autoSign) {
            progressMonitor.processName = globalClass.resources.getString(R.string.signing)
            signApkFile(mergedFile)
        }

        if (progressMonitor.status == TaskStatus.RUNNING) {
            progressMonitor.status = TaskStatus.SUCCESS
            progressMonitor.summary =
                globalClass.resources.getString(R.string.apk_bundle_merge_success)
        }
    }

    fun mergeBundleFile(holder: LocalFileHolder): LocalFileHolder? {
        val inputFile = holder.file

        if (inputFile.parent == null) {
            markAsFailed(globalClass.resources.getString(R.string.invalid_bundle_file))
            return null
        }

        val outputFile = if (parameters!!.autoSign) {
            File.createTempFile(inputFile.nameWithoutExtension, ".apk")
        } else {
            File(inputFile.parentFile, inputFile.nameWithoutExtension + "-unsigned.apk")
        }

        val options = MergerOptions().apply {
            this.inputFile = inputFile
            this.outputFile = outputFile
        }

        try {
            Merger(options).runCommand()
        } catch (e: Exception) {
            logger.logError(e)
            markAsFailed(
                globalClass.resources.getString(
                    R.string.task_summary_failed,
                    e.message ?: emptyString
                )
            )
            return null
        }

        if (outputFile.exists() && outputFile.length() > 0) {
            return LocalFileHolder(outputFile)
        }

        markAsFailed(globalClass.resources.getString(R.string.apk_bundle_merge_failed))
        return null
    }

    fun signApkFile(mergedFile: LocalFileHolder) {
        val sourceFile = sourceContent as LocalFileHolder

        val inputFile = mergedFile.file

        if (sourceFile.file.parent == null) {
            markAsFailed(globalClass.resources.getString(R.string.invalid_bundle_file))
            return
        }

        val outputFile = File(
            sourceFile.file.parentFile,
            sourceFile.file.nameWithoutExtension + "-signed.apk"
        )

        try {
            val testKeyInputStream = globalClass.assets.open("keystore/testkey.pk8")
            val certificateInputStream = globalClass.assets.open("keystore/testkey.x509.pem")

            val testKey = testKeyInputStream.use { inputStream ->
                val keyBytes = inputStream.readBytes()
                val keySpec = PKCS8EncodedKeySpec(keyBytes)
                val keyFactory = KeyFactory.getInstance("RSA")
                keyFactory.generatePrivate(keySpec)
            }
            val certificate = certificateInputStream.use { inputStream ->
                val certificateFactory = CertificateFactory.getInstance("X.509")
                certificateFactory.generateCertificate(inputStream) as X509Certificate
            }

            val keyConfig = KeyConfig.Jca(testKey)

            val signerConfig = ApkSigner.SignerConfig.Builder(
                "Android",
                keyConfig,
                listOf(certificate)
            ).build()

            val apkSigner = ApkSigner.Builder(listOf(signerConfig))
                .setInputApk(inputFile)
                .setOutputApk(outputFile)
                .setV1SigningEnabled(true)
                .setV2SigningEnabled(true)
                .build()
                .sign()

            if (!outputFile.exists() || outputFile.length() == 0L) {
                markAsFailed(globalClass.resources.getString(R.string.apk_bundle_sign_failed))
                return
            }

            inputFile.delete()
        } catch (e: Exception) {
            logger.logError(e)
            markAsFailed(
                globalClass.resources.getString(
                    R.string.task_summary_failed,
                    e.message ?: emptyString
                )
            )
        }
    }

    override suspend fun continueTask() {
        if (parameters == null) {
            markAsFailed(globalClass.getString(R.string.unable_to_continue_task))
            return
        }
        run(parameters!!)
    }

}
