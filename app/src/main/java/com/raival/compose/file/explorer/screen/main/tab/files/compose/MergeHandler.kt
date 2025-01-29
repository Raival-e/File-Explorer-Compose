package com.raival.compose.file.explorer.screen.main.tab.files.compose

import android.content.Context
import com.android.apksig.ApkSigner
import com.android.apksig.ApkSigner.SignerConfig
import com.android.apksig.KeyConfig
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.modal.DocumentHolder
import com.reandroid.apkeditor.merge.Merger
import com.reandroid.apkeditor.merge.MergerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.SignatureException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec

class MergeHandler(private val context: Context) {

    @Throws(IOException::class)
    fun executeMerge(inputFilePath: String, outputFilePath: String, onProgressUpdate: (Float, String) -> Unit) {
        val options = MergerOptions().apply {
            inputFile = File(inputFilePath)
            outputFile = File(outputFilePath)
        }
        val merger = Merger(options)
        val tasks = listOf(
            context.getString(R.string.initializing_merge) to 0.1f,
            context.getString(R.string.extracting_files) to 0.3f,
            context.getString(R.string.merging_modules) to 0.6f,
            context.getString(R.string.finalizing_merge) to 0.8f,
        )
        for ((message, progress) in tasks) {
            Thread.sleep(500)
            onProgressUpdate(progress, message)
        }
        merger.runCommand()
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class, InvalidKeySpecException::class, SignatureException::class)
    private fun signApk(apkFilePath: String, outFilePath: String, keyInputStream: InputStream, certInputStream: InputStream) {
        val privateKey = keyInputStream.use { inputStream ->
            val keyBytes = inputStream.readBytes()
            val keySpec = PKCS8EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePrivate(keySpec)
        }

        val certificate = certInputStream.use { inputStream ->
            val certificateFactory = CertificateFactory.getInstance("X.509")
            certificateFactory.generateCertificate(inputStream) as X509Certificate
        }

        val keyConfig = KeyConfig.Jca(privateKey)

        val signerConfig = SignerConfig.Builder(
            "Android",
            keyConfig,
            listOf(certificate)
        ).build()

        val apkSigner = ApkSigner.Builder(listOf(signerConfig))
            .setInputApk(File(apkFilePath))
            .setOutputApk(File(outFilePath))
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .build()

        apkSigner.sign()
    }

    fun mergeApks(tab: FilesTab, apkFile: DocumentHolder, doSign: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fileExtension = apkFile.fileExtension
                val fileName = apkFile.getName().removeSuffix(".$fileExtension")
                val filePath = apkFile.path
                val fileDir = apkFile.parent?.path
                val outputFilePath = "$fileDir/$fileName.apk"
                val mergeHandler = MergeHandler(context)
                withContext(Dispatchers.Main) {
                    tab.taskDialog.showTaskDialog = true
                    tab.taskDialog.taskDialogInfo = context.getString(R.string.merging_apks)
                    tab.taskDialog.taskDialogTitle = context.getString(R.string.merging)
                    tab.taskDialog.taskDialogSubtitle = context.getString(R.string.merging_apks)
                    tab.taskDialog.showTaskDialogProgressbar = true
                    tab.taskDialog.taskDialogProgress = 0f
                }
                mergeHandler.executeMerge(filePath, outputFilePath) { progress, message ->
                    launch(Dispatchers.Main) {
                        tab.taskDialog.taskDialogProgress = progress
                        tab.taskDialog.taskDialogSubtitle = message
                    }
                }
                val outputFile = File(outputFilePath)
                if (!outputFile.exists()) {
                    withContext(Dispatchers.Main) { onError(context.getString(R.string.merge_failed)) }
                    return@launch
                }

                if (doSign) {
                    // TODO: Custom KeyStore
                    val keyFilePath = tab.assetManager.open("keystore/testkey.pk8")
                    val certFilePath = tab.assetManager.open("keystore/testkey.x509.pem")
                    val signedApkPath = outputFilePath.replace(".apk", "-signed.apk")

                    tab.taskDialog.taskDialogSubtitle = context.getString(R.string.signing_apk)
                    tab.taskDialog.taskDialogProgress = 0.9f
                    signApk(outputFilePath, signedApkPath, keyFilePath, certFilePath)

                    if (!File(signedApkPath).exists()) {
                        withContext(Dispatchers.Main) { onError(context.getString(R.string.signing_failed)) }
                        return@launch
                    }
                    if (File(outputFilePath).exists()) {
                        File(outputFilePath).delete()
                    }
                }

                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError(context.getString(R.string.merge_failed_with_error, e.message))
                    delay(500)
                    tab.taskDialog.showTaskDialog = false
                }
            }
        }
    }
}
