package com.raival.compose.file.explorer.screen.main.tab.files

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider.getUriForFile
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.getIndexIf
import com.raival.compose.file.explorer.common.getMimeType
import com.raival.compose.file.explorer.common.isNot
import com.raival.compose.file.explorer.common.orIf
import com.raival.compose.file.explorer.common.removeIf
import com.raival.compose.file.explorer.screen.main.MainActivity
import com.raival.compose.file.explorer.screen.main.tab.Tab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ZipFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileListCategory
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.anyFileType
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.apkFileType
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider
import com.raival.compose.file.explorer.screen.main.tab.files.state.BottomOptionsBarState
import com.raival.compose.file.explorer.screen.main.tab.files.state.DialogsState
import com.raival.compose.file.explorer.screen.main.tab.files.task.CompressTask
import com.reandroid.archive.ZipAlign
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import java.io.File

class FilesTab(
    val source: ContentHolder,
    context: Context? = null
) : Tab() {
    companion object {
        fun isValidLocalPath(path: String) = File(path).exists()
    }

    override val id = globalClass.generateUid()
    val scope = CoroutineScope(Dispatchers.IO)

    val homeDir: ContentHolder =
        if (source is VirtualFileHolder || source.isFolder) source else runBlocking { source.getParent() }
            ?: StorageProvider.getPrimaryInternalStorage(globalClass).contentHolder

    var activeFolder: ContentHolder = homeDir
    var activeFolderContent = mutableStateListOf<ContentHolder>()
    val contentListStates = hashMapOf<String, LazyGridState>()
    var activeListState by mutableStateOf(LazyGridState())

    var viewConfig by mutableStateOf(
        globalClass.preferencesManager.getViewConfigPrefsFor(activeFolder)
    )

    val highlightedFiles = arrayListOf<String>()
    val selectedFiles = linkedMapOf<String, ContentHolder>()
    var lastSelectedFileIndex = -1

    var currentPathSegments by mutableStateOf(listOf<ContentHolder>())
    var highlightedPathSegment by mutableStateOf(activeFolder)
    val currentPathSegmentsListState = LazyListState()

    var categories = mutableStateListOf<FileListCategory>()
    var selectedCategory by mutableStateOf<FileListCategory?>(null)

    // Holds the file that has been long-clicked
    var targetFile: ContentHolder? = null
    var compressTaskHolder: CompressTask? = null

    // Used to detect changes to an already updated ZipTree in ZipManager
    var zipSourceTimestamp = -1L

    private val _dialogsState = MutableStateFlow(DialogsState())
    val dialogsState = _dialogsState.asStateFlow()

    private val _bottomOptionsBarState = MutableStateFlow(BottomOptionsBarState())
    val bottomOptionsBarState = _bottomOptionsBarState.asStateFlow()

    var handleBackGesture by mutableStateOf(true)
    var tabViewLabel by mutableStateOf(homeDir.displayName)

    var isLoading by mutableStateOf(false)

    private var foldersCount = 0
    private var filesCount = 0

    init {
        // If the tab point to a file, open it immediately without waiting for its parent content to be loaded
        if (source.isFile()) {
            context?.let { openFile(context, source) }
        }
    }

    override fun onTabStarted() {
        super.onTabStarted()
        // Load either the source file or the home tab
        scope.launch {
            if (source.isFile()) { // This is true when locating a file
                // Check if we can access the parent folder and open it
                source.getParent()?.let { parent ->
                    openFolderImpl(parent) {
                        // Scroll to the file once the content has been loaded
                        CoroutineScope(Dispatchers.Main).launch {
                            getFileListState().scrollToItem(
                                maxOf(
                                    activeFolderContent.getIndexIf { uniquePath == source.uniquePath },
                                    0
                                ),
                                0
                            )
                        }
                    }
                } ?: also {
                    // If parent folder cannot be accessed, revert to home folder
                    openFolderImpl(homeDir)
                }

                // Highlight the opened file
                highlightedFiles.apply {
                    clear()
                    add(source.uniquePath)
                }
            } else {
                // If the source file is a folder, open it
                openFolderImpl(homeDir)
            }
        }
    }

    override fun onTabResumed() {
        scope.launch {
            validateActiveFolder()
            // Important to clear any information from previous tabs (when switching tabs)
            requestHomeToolbarUpdate()
            // Detect any content changes
            detectFileChanges()
            // Check for display mode change
            updateDisplayConfig()
        }
    }

    override val header: String
        get() = tabViewLabel

    override suspend fun getTitle(): String {
        return createTitle()
    }

    override suspend fun getSubtitle(): String {
        return createSubtitle()
    }

    private suspend fun createSubtitle(): String {
        var selectedFolders = 0
        var selectedFiles = 0

        this.selectedFiles.values.forEach {
            if (it.isFolder) selectedFolders++
            else selectedFiles++
        }

        return buildString {
            if (foldersCount + filesCount == 0) {
                append(globalClass.getString(R.string.empty_folder))
                return@buildString
            }

            if (foldersCount > 0) {
                append(activeFolder.getFormattedFileCount(0, foldersCount))
                if (selectedFolders > 0) {
                    append(globalClass.getString(R.string.files_selected).format(selectedFolders))
                }
            }

            if (filesCount > 0 && foldersCount > 0) append(", ")

            if (filesCount > 0) {
                append(activeFolder.getFormattedFileCount(filesCount, 0))
                if (selectedFiles > 0) {
                    append(globalClass.getString(R.string.files_selected).format(selectedFiles))
                }
            }
        }
    }

    private suspend fun createTitle() = globalClass.getString(R.string.files_tab_title)

    override fun onBackPressed(): Boolean {
        if (unselectAnySelectedFiles()) {
            return true
        } else if (handleBackGesture) {
            scope.launch {
                highlightedFiles.apply {
                    clear()
                    add(activeFolder.uniquePath)
                }
                openFolderImpl(activeFolder.getParent()!!)
            }

            return true
        }

        return false
    }

    /**
     * Unselects all the selected files.
     * returns true if any files were selected
     */
    fun unselectAnySelectedFiles(): Boolean {
        if (selectedFiles.isNotEmpty()) {
            unselectAllFiles()
            return true
        }
        return false
    }


    fun unselectAllFiles(quickReload: Boolean = true) {
        selectedFiles.clear()
        lastSelectedFileIndex = -1
        if (quickReload) quickReloadFiles()
    }

    fun openFile(context: Context, item: ContentHolder) {
        if (item is LocalFileHolder && item.isApk()) {
            toggleApkDialog(item)
        } else {
            item.open(
                context = context,
                anonymous = false,
                skipSupportedExtensions = !globalClass.preferencesManager.useBuiltInViewer,
                customMimeType = null
            )
        }
    }

    fun openFolder(
        item: ContentHolder,
        rememberListState: Boolean = true,
        rememberSelectedFiles: Boolean = false,
        postEvent: () -> Unit = {}
    ) {
        scope.launch {
            openFolderImpl(item, rememberListState, rememberSelectedFiles, postEvent)
        }
    }

    suspend fun openFolderImpl(
        item: ContentHolder,
        rememberListState: Boolean = true,
        rememberSelectedFiles: Boolean = false,
        postEvent: () -> Unit = {}
    ) {
        // Block UI
        if (isLoading) return

        // Prevent opening invalid files
        if (!item.isValid()) return

        // For virtual folders, update the category
        if (item is VirtualFileHolder) {
            item.selectedCategory = selectedCategory
        }

        // Switch to the new folder
        activeFolder = item

        // Update header label
        withContext(Dispatchers.Main) {
            updateTabViewLabel()
        }

        // Clear selection if not needed
        if (!rememberSelectedFiles) {
            selectedFiles.clear()
            lastSelectedFileIndex = -1
        } else {
            // Otherwise, validate the selection
            selectedFiles.removeIf { key, value -> runBlocking { !value.isValid() } }
            if (selectedFiles.isEmpty()) lastSelectedFileIndex = -1
        }

        // Update the bottom bar options to fit the new folder
        _bottomOptionsBarState.update {
            it.copy(
                showQuickOptions = selectedFiles.isNotEmpty(),
                showCreateNewContentButton = activeFolder.canAddNewContent,
                showMoreOptionsButton = selectedFiles.isNotEmpty(),
                showEmptyRecycleBinButton = activeFolder is LocalFileHolder &&
                        ((activeFolder as LocalFileHolder).hasParent(globalClass.recycleBinDir) ||
                                activeFolder.uniquePath == globalClass.recycleBinDir.uniquePath)
            )
        }

        // Check if the back gesture can be handled
        handleBackGesture = runBlocking { activeFolder.hasParent() || selectedFiles.isNotEmpty() }

        // Update the path list
        updatePathList()

        // Update the zipSourceTimestamp if the active folder is a zip file
        if (activeFolder is ZipFileHolder) {
            zipSourceTimestamp = (activeFolder as ZipFileHolder).zipTree.timeStamp
        }

        // Get the content of the new folder
        listFiles { newContent -> // Main thread
            // Update the active folder content
            activeFolderContent.clear()
            activeFolderContent.addAll(newContent)

            // Once the content has been loaded, update the home toolbar title and subtitle
            requestHomeToolbarUpdate()

            // If a new folder is opened, the list must be at the starting position,
            // but when navigating back to parent folder, the saved location must be maintained
            if (!rememberListState) {
                contentListStates[item.uniquePath] = LazyGridState(0, 0)
            }

            // Update the active list state
            activeListState = contentListStates[item.uniquePath] ?: LazyGridState()
                .also { contentListStates[item.uniquePath] = it }

            // Get display config for this folder
            updateDisplayConfig()

            // Update the categories
            if (activeFolder is VirtualFileHolder) {
                categories.clear()
                categories.addAll((activeFolder as VirtualFileHolder).getCategories())
            }

            // Call any posted events
            postEvent()
        }
    }

    suspend fun validateActiveFolder() {
        if (!activeFolder.isValid()) {
            // Try to find a valid parent folder
            var validParent: ContentHolder? = null
            var current = activeFolder

            while (current.hasParent()) {
                current = current.getParent() ?: break
                if (current.isValid()) {
                    validParent = current
                    break
                }
            }
            validParent?.let {
                openFolder(it)
                return
            }

            if (homeDir.isValid()) {
                openFolder(homeDir)
            } else {
                openFolder(StorageProvider.getPrimaryInternalStorage(globalClass).contentHolder)
            }
        }
    }

    suspend fun detectFileChanges(): Boolean {
        // terminate the check if the tab is busy
        if (isLoading) return false

        // check if any file has been changed
        if (activeFolder is LocalFileHolder) {
            val newContent =
                (activeFolder as LocalFileHolder).file.listFiles()?.toCollection(arrayListOf())
                    ?.apply {
                        if (!globalClass.preferencesManager.showHiddenFiles) {
                            removeIf { it.name.startsWith(".") }
                        }
                    }
            // Check if the content size has changed
            if (newContent != null && newContent.size != activeFolderContent.size) {
                reloadFiles()
                return true
            }
            // Check each file to see if the source has changed
            if (activeFolderContent.any { (it as LocalFileHolder).hasSourceChanged() }) {
                reloadFiles()
                return true
            }
        } else if (activeFolder is ZipFileHolder) {
            val invalidZipTrees = globalClass.zipManager.validateArchiveTrees()
            if (invalidZipTrees.contains((activeFolder as ZipFileHolder).zipTree.source.uniquePath)) {
                validateActiveFolder()
            } else if (globalClass.zipManager.checkForSourceChanges()) {
                // Check if the source any of the files that have been extracted has changed
                val zipTree = (activeFolder as ZipFileHolder).zipTree
                val changedFiles = zipTree.checkExtractedFiles()
                if (changedFiles.isNotEmpty()) {
                    isLoading = true
                    ZipFile(zipTree.source.file).use { zipFile ->
                        changedFiles.forEach { changedFile ->
                            zipTree.getRelatedNode(changedFile)?.let { node ->
                                zipFile.addFile(
                                    changedFile.file,
                                    ZipParameters().apply {
                                        fileNameInZip = node.path
                                        isOverrideExistingFilesInZip = true
                                    }
                                )
                            }
                            changedFile.resetCachedTimestamp()
                        }
                    }
                    if (zipTree.source.extension == apkFileType) {
                        try {
                            ZipAlign.alignApk(zipTree.source.file)
                        } catch (e: Exception) {
                            logger.logError(e)
                        }
                    }
                    isLoading = false
                }
                zipTree.reset()
                reloadFiles()
                return true
            } else if (zipSourceTimestamp isNot (activeFolder as ZipFileHolder).zipTree.timeStamp) {
                reloadFiles()
                return true
            }
        }
        return false
    }

    fun quickReloadFiles() {
        scope.launch {
            // terminate if the tab is busy
            if (isLoading) return@launch

            // Put the content in a temporary list
            val temp = arrayListOf<ContentHolder>().apply { addAll(activeFolderContent) }

            // Recheck the back gesture
            handleBackGesture = activeFolder.hasParent() || selectedFiles.isNotEmpty()

            // Update the bottom bar options
            _bottomOptionsBarState.update {
                it.copy(
                    showQuickOptions = selectedFiles.isNotEmpty(),
                    showMoreOptionsButton = selectedFiles.isNotEmpty(),
                    showEmptyRecycleBinButton = activeFolder is LocalFileHolder &&
                            ((activeFolder as LocalFileHolder).hasParent(globalClass.recycleBinDir) ||
                                    activeFolder.uniquePath == globalClass.recycleBinDir.uniquePath)
                )
            }

            withContext(Dispatchers.Main) {
                // Reload the list
                activeFolderContent.clear()
                activeFolderContent.addAll(temp)

                // Update title and subtitle
                requestHomeToolbarUpdate()
            }
        }
    }

    fun onSelectionChange() {
        scope.launch {
            // Recheck the back gesture
            handleBackGesture = activeFolder.hasParent() || selectedFiles.isNotEmpty()

            // Update the bottom bar options
            _bottomOptionsBarState.update {
                it.copy(
                    showQuickOptions = selectedFiles.isNotEmpty(),
                    showMoreOptionsButton = selectedFiles.isNotEmpty(),
                    showEmptyRecycleBinButton = activeFolder is LocalFileHolder &&
                            ((activeFolder as LocalFileHolder).hasParent(globalClass.recycleBinDir) ||
                                    activeFolder.uniquePath == globalClass.recycleBinDir.uniquePath)
                )
            }

            // Update title and subtitle
            requestHomeToolbarUpdate()
        }
    }

    fun reloadFiles(postEvent: () -> Unit = {}) {
        scope.launch {
            openFolderImpl(activeFolder) { postEvent() }
        }
    }

    private suspend fun listFiles(onReady: (ArrayList<out ContentHolder>) -> Unit) {
        isLoading = true

        val result = activeFolder.listSortedContent()

        activeFolder.getContentCount().let { contentCount ->
            foldersCount = contentCount.folders
            filesCount = contentCount.files
        }

        withContext(Dispatchers.Main) {
            onReady(result)
            isLoading = false
        }
    }

    // Called when a new file/folder is created
    fun onNewFileCreated(newFile: ContentHolder, openFolder: Boolean = false) {
        scope.launch {
            if (openFolder) {
                openFolderImpl(newFile)
            } else {
                highlightedFiles.apply {
                    clear()
                    add(newFile.uniquePath)
                }

                reloadFiles {
                    CoroutineScope(Dispatchers.Main).launch {
                        val newItemIndex =
                            activeFolderContent.getIndexIf { displayName == newFile.displayName }
                        if (newItemIndex > -1) {
                            getFileListState().scrollToItem(newItemIndex, 0)
                        }
                    }
                }
            }
        }
    }

    fun getFileListState() = contentListStates[activeFolder.uniquePath] ?: LazyGridState().also {
        contentListStates[activeFolder.uniquePath] = it
    }

    private fun updateTabViewLabel() {
        val fullName =
            activeFolder.displayName.orIf(globalClass.getString(R.string.internal_storage)) {
                activeFolder.uniquePath == Environment.getExternalStorageDirectory().absolutePath
            }
        tabViewLabel = if (fullName.length > 18) fullName.substring(0, 15) + "..." else fullName
    }

    private suspend fun updatePathList() {
        // Walk through parent files
        val paths = generateSequence(activeFolder) {
            runBlocking { it.getParent() }
        }

        // Filter those that accessible, reverse the list
        val newPathSegments =
            paths.filter { it.canRead && runBlocking { it.isValid() } }.toList().reversed()

        if (!currentPathSegments.joinToString(emptyString) { it.displayName }.startsWith(
                newPathSegments.joinToString(emptyString) { it.displayName })
        ) {
            // Update the state to reflect the new path
            withContext(Dispatchers.Main) {
                currentPathSegments = newPathSegments
            }
        } else {
            // validate path segments if not changed
            currentPathSegments = currentPathSegments.filter { it.isValid() }
        }

        highlightedPathSegment = activeFolder
    }

    fun updateDisplayConfig() {
        viewConfig = globalClass.preferencesManager.getViewConfigPrefsFor(activeFolder)
    }

    fun requestNewTab(tab: Tab) {
        globalClass.mainActivityManager.addTabAndSelect(tab)
    }

    /**
     * Shares the selected files.
     * Only local content can be shared, other types must create a local copy first.
     */
    fun shareSelectedFiles(context: Context) {
        val uris = arrayListOf<Uri>()

        selectedFiles.forEach { selectedFile ->
            val content = selectedFile.component2()
            if (content is LocalFileHolder) {
                uris.add(
                    getUriForFile(context, globalClass.packageName + ".provider", content.file)
                )
            }
        }

        val builder = ShareCompat.IntentBuilder(globalClass)
            .setType(if (uris.size == 1) uris[0].getMimeType(globalClass) else anyFileType)
        uris.forEach {
            builder.addStream(it)
        }

        context.startActivity(
            builder.intent.apply {
                if (uris.size > 1) action = Intent.ACTION_SEND_MULTIPLE

                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
        )
    }

    fun addToHomeScreen(context: Context, file: LocalFileHolder) {
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        val pinShortcutInfo = ShortcutInfo
            .Builder(context, file.uniquePath)
            .setIntent(
                Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    putExtra("filePath", file.uniquePath)
                    flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                }
            )
            .setIcon(
                android.graphics.drawable.Icon.createWithResource(
                    context,
                    if (file.isFile()) R.mipmap.file_shortcut else R.mipmap.folder_shortcut
                )
            )
            .setShortLabel(file.displayName)
            .build()
        val pinnedShortcutCallbackIntent =
            shortcutManager.createShortcutResultIntent(pinShortcutInfo)
        shortcutManager.requestPinShortcut(
            pinShortcutInfo,
            PendingIntent.getBroadcast(
                context,
                0,
                pinnedShortcutCallbackIntent,
                PendingIntent.FLAG_IMMUTABLE
            ).intentSender
        )
    }

    fun extractZipHolderForPreview(zipFileHolder: ZipFileHolder, onDone: (String) -> Unit) {
        isLoading = true
        scope.launch {
            val newFilePath = zipFileHolder.extractForPreview()
            isLoading = false
            withContext(Dispatchers.Main) {
                onDone(newFilePath)
            }
        }
    }

    fun toggleApkDialog(file: LocalFileHolder?) {
        if (file isNot null) {
            targetFile = file
            _dialogsState.update { it.copy(showApkDialog = true) }
        } else {
            targetFile = null
            _dialogsState.update { it.copy(showApkDialog = false) }
        }
    }

    fun toggleFileOptionsMenu(file: ContentHolder?, clear: Boolean = true) {
        if (file isNot null) {
            targetFile = file
            _dialogsState.update { it.copy(showFileOptionsDialog = true) }
        } else {
            if (clear) targetFile = null
            _dialogsState.update { it.copy(showFileOptionsDialog = false) }
        }
    }

    fun toggleCompressTaskDialog(task: CompressTask?) {
        if (task isNot null) {
            compressTaskHolder = task
            _dialogsState.update { it.copy(showNewZipFileDialog = true) }
        } else {
            _dialogsState.update { it.copy(showNewZipFileDialog = false) }
        }
    }

    fun toggleBookmarksDialog(show: Boolean) {
        _dialogsState.update { it.copy(showBookmarkDialog = show) }
    }

    fun toggleDeleteConfirmationDialog(show: Boolean) {
        _dialogsState.update { it.copy(showConfirmDeleteDialog = show) }
    }

    fun toggleCreateNewFileDialog(show: Boolean) {
        _dialogsState.update { it.copy(showCreateNewFileDialog = show) }
    }

    fun toggleTasksPanel(show: Boolean) {
        _dialogsState.update { it.copy(showTasksPanel = show) }
    }

    fun toggleSortingMenu(show: Boolean) {
        _dialogsState.update { it.copy(showSortingMenu = show) }
    }

    fun toggleViewConfigDialog(show: Boolean) {
        _dialogsState.update { it.copy(showViewConfigDialog = show) }
    }

    fun toggleSearchPenal(show: Boolean) {
        _dialogsState.update { it.copy(showSearchPenal = show) }
    }

    fun toggleRenameDialog(show: Boolean) {
        _dialogsState.update { it.copy(showRenameDialog = show) }
    }

    fun toggleOpenWithDialog(show: Boolean) {
        _dialogsState.update { it.copy(showOpenWithDialog = show) }
    }

    fun toggleFilePropertiesDialog(show: Boolean) {
        _dialogsState.update { it.copy(showFileProperties = show) }
    }

    fun toggleImportPrefsDialog(file: LocalFileHolder?) {
        targetFile = file
        _dialogsState.update { it.copy(showImportPrefsDialog = file != null) }
    }
}