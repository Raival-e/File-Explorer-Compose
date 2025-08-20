package com.raival.compose.file.explorer.screen.main.tab.files.task

import com.android.apksig.ApkSigner
import com.android.apksig.KeyConfig
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.toFormattedDate
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
            isCancellable = true,
            canMoveToBackground = false
        )
    }

    override val progressMonitor = TaskProgressMonitor(
        status = TaskStatus.PENDING,
        taskTitle = metadata.title,
    )

    override fun getCurrentStatus() = progressMonitor.status

    override suspend fun validate() = sourceContent.isValid()

    private fun markAsFailed(info: String) {
        progressMonitor.apply {
            status = TaskStatus.FAILED
            summary = info
            progress = 0f
        }
    }

    private fun markAsAborted() {
        progressMonitor.apply {
            status = TaskStatus.PAUSED
            summary = globalClass.getString(R.string.task_aborted)
        }
    }

    override suspend fun run() {
        if (parameters == null) {
            markAsFailed(globalClass.getString(R.string.unable_to_continue_task))
            return
        }
        run(parameters!!)
    }

    override suspend fun run(params: TaskParameters) {
        parameters = params as ApksMergeTaskParameters
        progressMonitor.status = TaskStatus.RUNNING
        protect = false

        // Check abortion before validation
        if (aborted) {
            markAsAborted()
            return
        }

        if (!sourceContent.isValid() || sourceContent !is LocalFileHolder) {
            markAsFailed(globalClass.resources.getString(R.string.invalid_bundle_file))
            return
        }

        progressMonitor.apply {
            processName = globalClass.resources.getString(R.string.merging)
            progress = 0.1f
        }

        // Check abortion before merging
        if (aborted) {
            markAsAborted()
            return
        }

        val mergedFile = mergeBundleFile(sourceContent)

        // Check abortion after merging
        if (aborted) {
            // Clean up merged file if created
            mergedFile?.file?.delete()
            markAsAborted()
            return
        }

        // mergeBundleFile function will handle failed task
        if (mergedFile != null && parameters!!.autoSign) {
            progressMonitor.apply {
                processName = globalClass.resources.getString(R.string.signing)
                progress = 0.7f
            }

            // Check abortion before signing
            if (aborted) {
                mergedFile.file.delete()
                markAsAborted()
                return
            }

            signApkFile(mergedFile)
        }

        if (progressMonitor.status == TaskStatus.RUNNING) {
            progressMonitor.apply {
                status = TaskStatus.SUCCESS
                progress = 1.0f
                summary = globalClass.resources.getString(R.string.apk_bundle_merge_success)
                processName = globalClass.getString(R.string.completed)
            }
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

        progressMonitor.apply {
            progress = 0.3f
        }

        val options = MergerOptions().apply {
            this.inputFile = inputFile
            this.outputFile = outputFile
        }

        try {
            // Check abortion before starting merge
            if (aborted) {
                outputFile.delete()
                return null
            }

            Merger(options).runCommand()

            progressMonitor.apply {
                progress = 0.6f
            }
        } catch (e: Exception) {
            logger.logError(e)
            outputFile.delete()
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

        outputFile.delete()
        markAsFailed(globalClass.resources.getString(R.string.apk_bundle_merge_failed))
        return null
    }

    fun signApkFile(mergedFile: LocalFileHolder) {
        val sourceFile = sourceContent as LocalFileHolder
        val inputFile = mergedFile.file

        if (sourceFile.file.parent == null) {
            inputFile.delete()
            markAsFailed(globalClass.resources.getString(R.string.invalid_bundle_file))
            return
        }

        val outputFile = File(
            sourceFile.file.parentFile,
            sourceFile.file.nameWithoutExtension + "-signed.apk"
        )

        progressMonitor.apply {
            progress = 0.8f
        }

        try {
            // Check abortion before loading certificates
            if (aborted) {
                inputFile.delete()
                return
            }

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

            progressMonitor.apply {
                processName = globalClass.getString(R.string.signing)
                progress = 0.9f
            }

            // Check abortion before signing
            if (aborted) {
                inputFile.delete()
                return
            }

            val keyConfig = KeyConfig.Jca(testKey)

            val signerConfig = ApkSigner.SignerConfig.Builder(
                "Android",
                keyConfig,
                listOf(certificate)
            ).build()

            ApkSigner.Builder(listOf(signerConfig))
                .setInputApk(inputFile)
                .setOutputApk(outputFile)
                .setV1SigningEnabled(true)
                .setV2SigningEnabled(true)
                .build()
                .sign()

            if (!outputFile.exists() || outputFile.length() == 0L) {
                inputFile.delete()
                outputFile.delete()
                markAsFailed(globalClass.resources.getString(R.string.apk_bundle_sign_failed))
                return
            }

            // Clean up temp file after successful signing
            inputFile.delete()

        } catch (e: Exception) {
            logger.logError(e)
            inputFile.delete()
            outputFile.delete()
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

    override fun setParameters(params: TaskParameters) {
        parameters = params as ApksMergeTaskParameters
    }
}