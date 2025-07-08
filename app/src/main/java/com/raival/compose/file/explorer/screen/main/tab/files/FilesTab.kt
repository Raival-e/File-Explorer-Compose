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
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.getIndexIf
import com.raival.compose.file.explorer.common.extension.getMimeType
import com.raival.compose.file.explorer.common.extension.orIf
import com.raival.compose.file.explorer.common.extension.removeIf
import com.raival.compose.file.explorer.screen.main.MainActivity
import com.raival.compose.file.explorer.screen.main.tab.Tab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.VirtualFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ZipFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileMimeType.anyFileType
import com.raival.compose.file.explorer.screen.main.tab.files.provider.StorageProvider
import com.raival.compose.file.explorer.screen.main.tab.files.task.CompressTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FilesTab(
    val source: ContentHolder,
    context: Context? = null
) : Tab() {

    override val id = globalClass.generateUid()

    companion object {
        fun isValidLocalPath(path: String) = File(path).exists()
    }

    val search = Search()
    val apkDialog = ApkDialog()
    val fileOptionsDialog = FileOptionsDialog()
    val openWithDialog = OpenWithDialog()
    val renameDialog = RenameDialog()
    val newZipFileDialog = NewZipFileDialog()

    val homeDir: ContentHolder =
        if (source is VirtualFileHolder || source.isFolder) source else source.getParent()
            ?: StorageProvider.getPrimaryInternalStorage(globalClass).contentHolder
    var activeFolder: ContentHolder = homeDir

    val activeFolderContent = mutableStateListOf<ContentHolder>()
    val contentListStates = hashMapOf<String, LazyGridState>()
    var activeListState by mutableStateOf(LazyGridState())

    val currentPathSegments = mutableStateListOf<ContentHolder>()
    val currentPathSegmentsListState = LazyListState()

    val selectedFiles = linkedMapOf<String, ContentHolder>()
    var lastSelectedFileIndex = -1

    var highlightedFiles = arrayListOf<String>()

    var showSortingMenu by mutableStateOf(false)
    var showCreateNewFileDialog by mutableStateOf(false)
    var showConfirmDeleteDialog by mutableStateOf(false)
    var showFileProperties by mutableStateOf(false)
    var showTasksPanel by mutableStateOf(false)
    var showSearchPenal by mutableStateOf(false)
    var showBookmarkDialog by mutableStateOf(false)
    var showMoreOptionsButton by mutableStateOf(false)
    var showEmptyRecycleBin by mutableStateOf(false)
    var canCreateNewContent by mutableStateOf(true)
    var handleBackGesture by mutableStateOf(activeFolder.hasParent() || selectedFiles.isNotEmpty())
    var tabViewLabel by mutableStateOf(emptyString)

    var isLoading by mutableStateOf(false)

    var foldersCount = 0
    var filesCount = 0

    override fun onTabStarted() {
        super.onTabStarted()
        if (source.isFile()) {
            source.getParent()?.let { parent ->
                openFolder(parent) {
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
                openFolder(homeDir)
            }

            highlightedFiles.apply {
                clear()
                add(source.uniquePath)
            }
        } else {
            openFolder(homeDir)
        }
    }

    override fun onTabResumed() {
        requestHomeToolbarUpdate()
        detectFileChanges()
    }

    override val title: String
        get() = createTitle()

    override val subtitle: String
        get() = createSubtitle()

    override val header: String
        get() = tabViewLabel

    init {
        if (source.isFile()) {
            context?.let { openFile(context, source) }
        }
    }

    private fun createSubtitle(): String {
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

            if (filesCount > 0 && foldersCount > 0) append(" | ")

            if (filesCount > 0) {
                append(activeFolder.getFormattedFileCount(filesCount, 0))
                if (selectedFiles > 0) {
                    append(globalClass.getString(R.string.files_selected).format(selectedFiles))
                }
            }
        }
    }

    private fun createTitle() = globalClass.getString(R.string.files_tab_title)

    override fun onBackPressed(): Boolean {
        if (unselectAnySelectedFiles()) {
            return true
        } else if (handleBackGesture) {
            highlightedFiles.apply {
                clear()
                add(activeFolder.uniquePath)
            }
            openFolder(activeFolder.getParent()!!)

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

    fun openFile(context: Context, item: ContentHolder) {
        if (item is LocalFileHolder && item.isApk()) {
            apkDialog.show(item)
        } else {
            item.open(context, anonymous = false, skipSupportedExtensions = false, null)
        }
    }

    fun openFolder(
        item: ContentHolder,
        rememberListState: Boolean = true,
        rememberSelectedFiles: Boolean = false,
        postEvent: () -> Unit = {}
    ) {
        if (isLoading) return

        if (!rememberSelectedFiles) {
            selectedFiles.clear()
            lastSelectedFileIndex = -1
        } else {
            selectedFiles.removeIf { key, value -> !value.isValid() }
            if (selectedFiles.isEmpty()) lastSelectedFileIndex = -1
        }

        showMoreOptionsButton = selectedFiles.size > 0

        activeFolder = item

        listFiles { newContent ->
            if (activeFolder is LocalFileHolder) {
                showEmptyRecycleBin =
                    (activeFolder as LocalFileHolder).hasParent(globalClass.recycleBinDir)
                            || activeFolder.uniquePath == globalClass.recycleBinDir.uniquePath
            } else {
                showEmptyRecycleBin = false
            }

            canCreateNewContent = activeFolder.canAddNewContent


            handleBackGesture = activeFolder.hasParent() || selectedFiles.isNotEmpty()

            updatePathList()

            requestHomeToolbarUpdate()

            activeFolderContent.clear()
            activeFolderContent.addAll(newContent)

            if (!rememberListState) {
                contentListStates[item.uniquePath] = LazyGridState(0, 0)
            }

            activeListState = contentListStates[item.uniquePath] ?: LazyGridState()
                .also { contentListStates[item.uniquePath] = it }

            postEvent()
        }
    }

    fun detectFileChanges(): Boolean {
        if (isLoading) return false
        if (activeFolder is LocalFileHolder) {
            val newContent =
                (activeFolder as LocalFileHolder).file.listFiles()?.toCollection(arrayListOf())
                    ?.apply {
                        if (!globalClass.preferencesManager.fileListPrefs.showHiddenFiles) {
                            removeIf { it.name.startsWith(".") }
                        }
                    }
            if (newContent != null && newContent.size != activeFolderContent.size) {
                reloadFiles()
                return true
            }
            if (activeFolderContent.any { (it as LocalFileHolder).hasSourceChanged() }) {
                reloadFiles()
                return true
            }
        } else if (activeFolder is ZipFileHolder) {
            if (globalClass.zipManager.checkForSourceChanges()) {
                reloadFiles()
                return true
            }
        }
        return false
    }

    fun quickReloadFiles() {
        if (isLoading) return

        val temp = arrayListOf<ContentHolder>().apply { addAll(activeFolderContent) }

        activeFolderContent.clear()
        activeFolderContent.addAll(temp)

        handleBackGesture = activeFolder.hasParent() || selectedFiles.isNotEmpty()

        requestHomeToolbarUpdate()

        showMoreOptionsButton = selectedFiles.size > 0

        if (activeFolder is LocalFileHolder) {
            showEmptyRecycleBin =
                (activeFolder as LocalFileHolder).hasParent(globalClass.recycleBinDir)
                        || activeFolder.uniquePath == globalClass.recycleBinDir.uniquePath
        }
    }

    fun reloadFiles(postEvent: () -> Unit = {}) {
        openFolder(activeFolder) { postEvent() }
    }

    private fun listFiles(onReady: (ArrayList<out ContentHolder>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
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
    }

    fun unselectAllFiles(quickReload: Boolean = true) {
        selectedFiles.clear()
        lastSelectedFileIndex = -1
        if (quickReload) quickReloadFiles()
    }

    fun onNewFileCreated(newFile: ContentHolder, openFolder: Boolean = false) {
        if (openFolder) {
            openFolder(newFile)
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

    private fun updatePathList() {
        val path = generateSequence(activeFolder) { it.getParent() }

        val newPathSegments = path.filter { it.canRead }.toList().reversed()

        currentPathSegments.apply {
            clear()
            addAll(newPathSegments)
        }.also { updateTabViewLabel() }
    }

    fun requestNewTab(tab: Tab) {
        globalClass.mainActivityManager.addTabAndSelect(tab)
    }

    fun hideDocumentOptionsMenu() {
        fileOptionsDialog.hide()
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
                    if (file.isFile()) R.mipmap.default_shortcut_icon else R.mipmap.folder_shortcut_icon
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

    class ApkDialog {
        var showApkDialog by mutableStateOf(false)
            private set
        var apkFile: LocalFileHolder? = null
            private set

        fun show(file: LocalFileHolder) {
            apkFile = file
            showApkDialog = true
        }

        fun hide() {
            showApkDialog = false
        }
    }

    class FileOptionsDialog {
        var showFileOptionsDialog by mutableStateOf(false)
            private set
        var targetFile: ContentHolder? = null
            private set

        fun show(file: ContentHolder) {
            targetFile = file
            showFileOptionsDialog = true
        }

        fun hide() {
            showFileOptionsDialog = false
        }
    }

    class OpenWithDialog {
        var showOpenWithDialog by mutableStateOf(false)
            private set
        var targetFile: LocalFileHolder? = null
            private set

        fun show(file: LocalFileHolder) {
            targetFile = file
            showOpenWithDialog = true
        }

        fun hide() {
            showOpenWithDialog = false
        }
    }

    class NewZipFileDialog {
        var show by mutableStateOf(false)
            private set
        var task: CompressTask? = null
            private set

        fun show(task: CompressTask) {
            this.task = task
            show = true
        }

        fun hide() {
            show = false
        }
    }

    class RenameDialog {
        var show by mutableStateOf(false)
            private set
        var targetContent: ContentHolder? = null
            private set

        fun show(content: ContentHolder) {
            targetContent = content
            show = true
        }

        fun hide() {
            show = false
        }
    }

    class Search {
        var searchQuery by mutableStateOf(emptyString)
        var searchResults = mutableStateListOf<ContentHolder>()
    }
}