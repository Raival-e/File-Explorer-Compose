package com.raival.compose.file.explorer.screen.main.tab.files.provider

import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.nio.file.Files
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CalculationProgress(
    val isCalculating: Boolean = false,
    val current: Long = 0,
    val total: Long = 0,
    val message: String = emptyString
)

sealed interface PropertiesState {
    object Loading : PropertiesState

    data class SingleContentProperties(
        val name: String,
        val path: String,
        val type: String,
        val lastModified: String,
        val size: String,
        val permissions: String,
        val owner: String,
        val contentCount: StateFlow<String>,
        val checksum: StateFlow<String>,
        val sha256: StateFlow<String>,
        val contentProgress: StateFlow<CalculationProgress>,
        val checksumProgress: StateFlow<CalculationProgress>,
        val sha256Progress: StateFlow<CalculationProgress>,
    ) : PropertiesState

    data class MultipleContentProperties(
        val selectedFileCount: Int,
        val totalFileCount: StateFlow<String>,
        val totalSize: StateFlow<String>,
        val countProgress: StateFlow<CalculationProgress>,
        val sizeProgress: StateFlow<CalculationProgress>,
    ) : PropertiesState
}

data class ContentPropertiesUiState(
    val details: PropertiesState = PropertiesState.Loading
)

class ContentPropertiesProvider(private val contentHolders: List<ContentHolder>) {
    private val _uiState = MutableStateFlow(ContentPropertiesUiState())
    val uiState: StateFlow<ContentPropertiesUiState> = _uiState.asStateFlow()

    private val calculationScope = CoroutineScope(IO)
    private val activeJobs = mutableSetOf<Job>()

    init {
        when {
            contentHolders.isEmpty() -> {}

            contentHolders.size == 1 -> {
                loadSingleContentDetails(contentHolders.first())
            }

            else -> {
                loadMultipleContentDetails(contentHolders)
            }
        }
    }

    fun cleanup() {
        activeJobs.forEach { it.cancel() }
        calculationScope.cancel()
    }

    private fun loadSingleContentDetails(file: ContentHolder) {
        val contentCountFlow = MutableStateFlow(emptyString)
        val checksumFlow = MutableStateFlow(
            if (file.isFolder) emptyString else globalClass.getString(
                R.string.calculating
            )
        )
        val sha256Flow = MutableStateFlow(
            if (file.isFolder) emptyString else globalClass.getString(
                R.string.calculating
            )
        )
        val contentProgressFlow = MutableStateFlow(CalculationProgress())
        val checksumProgressFlow = MutableStateFlow(CalculationProgress())
        val sha256ProgressFlow = MutableStateFlow(CalculationProgress())

        _uiState.update {
            it.copy(
                details = PropertiesState.SingleContentProperties(
                    name = file.displayName.ifEmpty { globalClass.getString(R.string.root) },
                    path = file.getParent()?.uniquePath ?: File.separator,
                    type = determineFileType(file),
                    lastModified = formatTimestamp(file.lastModified),
                    size = if (file.isFolder) globalClass.getString(R.string.calculating) else formatFileSize(
                        file.size
                    ),
                    permissions = getPermissions(file),
                    owner = getOwner(file),
                    contentCount = contentCountFlow,
                    checksum = checksumFlow,
                    sha256 = sha256Flow,
                    contentProgress = contentProgressFlow,
                    checksumProgress = checksumProgressFlow,
                    sha256Progress = sha256ProgressFlow
                )
            )
        }

        if (file.isFolder) {
            val job = calculationScope.launch {
                contentProgressFlow.value = CalculationProgress(isCalculating = true)
                try {
                    val (fileCount, folderCount, totalSize) = calculateDirectoryStats(file) { current ->
                        contentProgressFlow.value = CalculationProgress(
                            isCalculating = true,
                            current = current
                        )
                    }
                    contentCountFlow.value =
                        globalClass.getString(R.string.files_folders_count, fileCount, folderCount)

                    // Update size for directories
                    _uiState.update { state ->
                        val currentDetails =
                            state.details as? PropertiesState.SingleContentProperties
                        currentDetails?.let {
                            state.copy(
                                details = it.copy(size = formatFileSize(totalSize))
                            )
                        } ?: state
                    }
                } catch (_: Exception) {
                    contentCountFlow.value = globalClass.getString(R.string.error_calculating)
                } finally {
                    contentProgressFlow.value = CalculationProgress(isCalculating = false)
                }
            }
            activeJobs.add(job)
        } else if (file is LocalFileHolder) {
            // Calculate checksum with progress
            val job = calculationScope.launch {
                checksumProgressFlow.value = CalculationProgress(isCalculating = true)
                try {
                    val checksum = calculateMD5WithProgress(file) { bytesProcessed, totalBytes ->
                        checksumProgressFlow.value = CalculationProgress(
                            isCalculating = true,
                            current = bytesProcessed,
                            total = totalBytes
                        )
                    }
                    checksumFlow.value = checksum
                } catch (_: Exception) {
                    checksumFlow.value = globalClass.getString(R.string.error_calculating)
                } finally {
                    checksumProgressFlow.value = CalculationProgress(isCalculating = false)
                }
            }
            activeJobs.add(job)
            // Calculate SHA-256 with progress
            val sha256Job = calculationScope.launch {
                sha256ProgressFlow.value = CalculationProgress(isCalculating = true)
                try {
                    val sha256 = calculateSha256(file) { bytesProcessed, totalBytes ->
                        sha256ProgressFlow.value = CalculationProgress(
                            isCalculating = true,
                            current = bytesProcessed,
                            total = totalBytes
                        )
                    }
                    sha256Flow.value = sha256
                } catch (_: Exception) {
                    sha256Flow.value = globalClass.getString(R.string.error_calculating)
                } finally {
                    sha256ProgressFlow.value = CalculationProgress(isCalculating = false)
                }
            }
            activeJobs.add(sha256Job)
        } else {
            checksumFlow.value = emptyString
        }
    }

    private fun loadMultipleContentDetails(files: List<ContentHolder>) {
        val totalFileCountFlow = MutableStateFlow(globalClass.getString(R.string.calculating))
        val totalSizeFlow = MutableStateFlow(globalClass.getString(R.string.calculating))
        val countProgressFlow = MutableStateFlow(CalculationProgress())
        val sizeProgressFlow = MutableStateFlow(CalculationProgress())

        _uiState.update {
            it.copy(
                details = PropertiesState.MultipleContentProperties(
                    selectedFileCount = files.size,
                    totalFileCount = totalFileCountFlow,
                    totalSize = totalSizeFlow,
                    countProgress = countProgressFlow,
                    sizeProgress = sizeProgressFlow
                )
            )
        }

        val job = calculationScope.launch {
            countProgressFlow.value =
                CalculationProgress(isCalculating = true, total = files.size.toLong())
            sizeProgressFlow.value =
                CalculationProgress(isCalculating = true, total = files.size.toLong())

            try {
                var totalFiles = 0L
                var totalFolders = 0L
                var totalSize = 0L
                var processedItems = 0L

                files.forEach { file ->
                    if (!isActive) return@forEach

                    processedItems++
                    countProgressFlow.value = CalculationProgress(
                        isCalculating = true,
                        current = processedItems,
                        total = files.size.toLong()
                    )
                    sizeProgressFlow.value = CalculationProgress(
                        isCalculating = true,
                        current = processedItems,
                        total = files.size.toLong()
                    )

                    if (file.isFolder) {
                        val (filesCount, foldersCount, dirSize) = calculateDirectoryStats(file)
                        totalFiles += filesCount
                        totalFolders += foldersCount + 1 // +1 for the directory itself
                        totalSize += dirSize
                    } else {
                        totalFiles++
                        totalSize += file.size
                    }

                    // Update intermediate results
                    totalFileCountFlow.value = globalClass.getString(
                        R.string.files_folders_count,
                        totalFiles,
                        totalFolders
                    )
                    totalSizeFlow.value = formatFileSize(totalSize)

                    yield() // Allow cancellation
                }
            } catch (_: Exception) {
                totalFileCountFlow.value = globalClass.getString(R.string.error_calculating)
                totalSizeFlow.value = globalClass.getString(R.string.error_calculating)
            } finally {
                countProgressFlow.value = CalculationProgress(isCalculating = false)
                sizeProgressFlow.value = CalculationProgress(isCalculating = false)
            }
        }
        activeJobs.add(job)
    }

    private fun determineFileType(file: ContentHolder): String {
        return when {
            file.isFolder -> globalClass.getString(R.string.folder)
            else -> {
                file.extension.lowercase().ifEmpty { globalClass.getString(R.string.unknown) }
            }
        }
    }

    private fun formatTimestamp(millis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm:ss a", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val z = (63 - java.lang.Long.numberOfLeadingZeros(bytes)) / 10
        return String.format(
            Locale.US,
            "%.1f %sB",
            bytes.toDouble() / (1L shl (z * 10)),
            " KMGTPE"[z]
        )
    }

    private fun getPermissions(file: ContentHolder): String {
        if (file is LocalFileHolder) {
            return buildString {
                append(if (file.canRead) "r" else "-")
                append(if (file.canWrite) "w" else "-")
                append(if (file.file.canExecute()) "x" else "-")
            }
        }

        return emptyString
    }

    private fun getOwner(file: ContentHolder): String {
        return try {
            if (file is LocalFileHolder) {
                Files.getOwner(file.file.toPath()).name
            } else {
                globalClass.getString(R.string.unknown)
            }
        } catch (_: Exception) {
            globalClass.getString(R.string.unknown)
        }
    }

    private suspend fun calculateDirectoryStats(
        directory: ContentHolder,
        onProgress: ((Long) -> Unit)? = null
    ): Triple<Long, Long, Long> {
        var fileCount = 0L
        var folderCount = 0L
        var totalSize = 0L
        var processed = 0L

        suspend fun processDirectory(dir: ContentHolder) {
            if (!currentCoroutineContext().isActive) return

            try {
                dir.listContent().forEach { file ->
                    if (!currentCoroutineContext().isActive) return@forEach

                    processed++
                    onProgress?.invoke(processed)

                    if (file.isFolder) {
                        folderCount++
                        processDirectory(file)
                    } else {
                        fileCount++
                        totalSize += file.size
                    }

                    if (processed % 50 == 0L) {
                        yield() // Periodically yield for cancellation
                    }
                }
            } catch (_: SecurityException) {
                // Skip directories we can't access
            }
        }

        processDirectory(directory)
        return Triple(fileCount, folderCount, totalSize)
    }

    private suspend fun calculateSha256(
        file: LocalFileHolder,
        onProgress: (Long, Long) -> Unit
    ): String {
        if (file.isFolder) return emptyString

        return withContext(IO) {
            val md = MessageDigest.getInstance("SHA-256")
            val fileSize = file.size
            var bytesProcessed = 0L

            try {
                FileInputStream(file.file).use { fis ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (fis.read(buffer).also { read = it } != -1) {
                        if (!currentCoroutineContext().isActive) break
                        md.update(buffer, 0, read)
                        bytesProcessed += read
                        onProgress(bytesProcessed, fileSize)

                        if (bytesProcessed % (64 * 1024) == 0L) {
                            yield() // Yield every 64KB
                        }
                    }
                }

                val digest = md.digest()
                val bigInt = BigInteger(1, digest)
                bigInt.toString(16).padStart(64, '0')
            } catch (_: Exception) {
                globalClass.getString(R.string.error_calculating)
            }
        }
    }

    private suspend fun calculateMD5WithProgress(
        file: LocalFileHolder,
        onProgress: (Long, Long) -> Unit
    ): String {
        if (file.isFolder) return emptyString

        return withContext(IO) {
            val md = MessageDigest.getInstance("MD5")
            val fileSize = file.size
            var bytesProcessed = 0L

            try {
                FileInputStream(file.file).use { fis ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (fis.read(buffer).also { read = it } != -1) {
                        if (!currentCoroutineContext().isActive) break
                        md.update(buffer, 0, read)
                        bytesProcessed += read
                        onProgress(bytesProcessed, fileSize)

                        if (bytesProcessed % (64 * 1024) == 0L) {
                            yield() // Yield every 64KB
                        }
                    }
                }

                val digest = md.digest()
                val bigInt = BigInteger(1, digest)
                bigInt.toString(16).padStart(32, '0')
            } catch (_: Exception) {
                globalClass.getString(R.string.error_calculating)
            }
        }
    }
}