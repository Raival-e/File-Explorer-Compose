package com.raival.compose.file.explorer.screen.preferences

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.common.emptyString
import com.raival.compose.file.explorer.common.fromJson
import com.raival.compose.file.explorer.common.toJson
import com.raival.compose.file.explorer.screen.main.startup.StartupTabs
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ContentHolder
import com.raival.compose.file.explorer.screen.main.tab.files.misc.DefaultOpeningMethods
import com.raival.compose.file.explorer.screen.main.tab.files.misc.FileSortingPrefs
import com.raival.compose.file.explorer.screen.main.tab.files.misc.SortingMethod.SORT_BY_NAME
import com.raival.compose.file.explorer.screen.main.tab.files.misc.ViewConfigs
import com.raival.compose.file.explorer.screen.main.tab.home.data.getDefaultHomeLayout
import com.raival.compose.file.explorer.screen.preferences.constant.FileItemSize
import com.raival.compose.file.explorer.screen.preferences.constant.ThemePreference
import com.raival.compose.file.explorer.screen.preferences.misc.prefDataStore
import com.raival.compose.file.explorer.screen.preferences.misc.prefMutableState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class PreferencesManager {
    val singleChoiceDialog = SingleChoiceDialog()

    //---------- Appearance -------------//
    var theme by prefMutableState(
        keyName = "theme",
        defaultValue = ThemePreference.SYSTEM.ordinal,
        getPreferencesKey = { intPreferencesKey(it) }
    )

    var showBottomBarLabels by prefMutableState(
        keyName = "showBottomBarLabels",
        defaultValue = true,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var homeTabLayout by prefMutableState(
        keyName = "homeTabLayout",
        defaultValue = getDefaultHomeLayout().toJson(),
        getPreferencesKey = { stringPreferencesKey(it) }
    )

    var dateTimeFormat by prefMutableState(
        keyName = "dateTimeFormat",
        defaultValue = "MMM dd, yyyy HH:mm:ss",
        getPreferencesKey = { stringPreferencesKey(it) }
    )

    var hideToolbar by prefMutableState(
        keyName = "hideToolbar",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )


    //---------- File List -------------//
    var itemSize by prefMutableState(
        keyName = "fileListSize",
        defaultValue = FileItemSize.MEDIUM.ordinal,
        getPreferencesKey = { intPreferencesKey(it) }
    )

    var showFolderContentCount by prefMutableState(
        keyName = "showFolderContentCount",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var showHiddenFiles by prefMutableState(
        keyName = "showHiddenFiles",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    //---------- Behavior -------------//
    var showFileOptionMenuOnLongClick by prefMutableState(
        keyName = "showFileOptionMenuOnLongClick",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var disablePullDownToRefresh by prefMutableState(
        keyName = "disablePullDownToRefresh",
        defaultValue = true,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var skipHomeWhenTabClosed by prefMutableState(
        keyName = "skipHomeWhenTabClosed",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var useBuiltInViewer by prefMutableState(
        keyName = "useBuiltInViewer",
        defaultValue = true,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var startupTabs by prefMutableState(
        keyName = "startupTabs",
        defaultValue = StartupTabs.default().toJson(),
        getPreferencesKey = { stringPreferencesKey(it) }
    )

    var bookmarks by prefMutableState(
        keyName = "bookmarks",
        defaultValue = emptySet(),
        getPreferencesKey = { stringSetPreferencesKey(it) }
    )

    var pinnedFiles by prefMutableState(
        keyName = "pinnedFiles",
        defaultValue = emptySet(),
        getPreferencesKey = { stringSetPreferencesKey(it) }
    )

    var excludedPathsFromRecentFiles by prefMutableState(
        keyName = "excludedPathFromRecentFiles",
        defaultValue = emptySet(),
        getPreferencesKey = { stringSetPreferencesKey(it) }
    )

    var removeHiddenPathsFromRecentFiles by prefMutableState(
        keyName = "removeHiddenPathsFromRecentFiles",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    //---------- File Operation -------------//
    var signMergedApkBundleFiles by prefMutableState(
        keyName = "signMergedApkBundleFiles",
        defaultValue = true,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var moveToRecycleBin by prefMutableState(
        keyName = "moveToRecycleBin",
        defaultValue = true,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var defaultOpeningMethods by prefMutableState(
        keyName = "defaultOpeningMethods",
        defaultValue = DefaultOpeningMethods().toJson(),
        getPreferencesKey = { stringPreferencesKey(it) }
    )

    //---------- Text Editor -------------//
    var pinLineNumber by prefMutableState(
        keyName = "pinLineNumber",
        defaultValue = true,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var symbolPairAutoCompletion by prefMutableState(
        keyName = "symbolPairAutoCompletion",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var autoIndent by prefMutableState(
        keyName = "autoIndent",
        defaultValue = true,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var enableMagnifier by prefMutableState(
        keyName = "enableMagnifier",
        defaultValue = true,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var useICULibToSelectWords by prefMutableState(
        keyName = "useICULibToSelectWords",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var deleteEmptyLineFast by prefMutableState(
        keyName = "deleteEmptyLineFast",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var deleteMultiSpaces by prefMutableState(
        keyName = "deleteMultiSpaces",
        defaultValue = true,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var recentFilesLimit by prefMutableState(
        keyName = "textEditorRecentFilesLimit",
        defaultValue = -1,
        getPreferencesKey = { intPreferencesKey(it) }
    )

    var readOnly by prefMutableState(
        keyName = "readOnly",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var wordWrap by prefMutableState(
        keyName = "wordWrap",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    //---------- Audio Player -------------//
    var autoPlayMusic by prefMutableState(
        keyName = "autoPlayMusic",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )


    // //---------- File Sorting -------------//
    var defaultSortMethod by prefMutableState(
        keyName = "filesSortingMethod",
        defaultValue = SORT_BY_NAME,
        getPreferencesKey = { intPreferencesKey(it) }
    )

    var showFoldersFirst by prefMutableState(
        keyName = "showFoldersFirst",
        defaultValue = true,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    var reverse by prefMutableState(
        keyName = "reverseFilesSortingMethod",
        defaultValue = false,
        getPreferencesKey = { booleanPreferencesKey(it) }
    )

    fun getSortingPrefsFor(content: ContentHolder): FileSortingPrefs {
        return runBlocking {
            fromJson(
                globalClass.prefDataStore.data.first()[stringPreferencesKey("fileSortingPrefs_${content.uniquePath}")]
            ) ?: getDefaultSortingPrefs()
        }
    }

    fun getDefaultSortingPrefs() = FileSortingPrefs(
        sortMethod = defaultSortMethod,
        showFoldersFirst = showFoldersFirst,
        reverseSorting = reverse
    )

    fun getDefaultViewConfigPrefs(): ViewConfigs {
        return runBlocking {
            fromJson(
                globalClass.prefDataStore.data.first()[stringPreferencesKey("viewConfigPrefs")]
            ) ?: ViewConfigs()
        }
    }

    fun setDefaultViewConfigPrefs(prefs: ViewConfigs) {
        runBlocking {
            globalClass.prefDataStore.edit {
                it[stringPreferencesKey("viewConfigPrefs")] = prefs.toJson()
            }
        }
    }

    fun getViewConfigPrefsFor(content: ContentHolder): ViewConfigs {
        return runBlocking {
            fromJson(
                globalClass.prefDataStore.data.first()[stringPreferencesKey("viewConfigPrefs_${content.uniquePath}")]
            ) ?: getDefaultViewConfigPrefs()
        }
    }

    fun setViewConfigPrefsFor(content: ContentHolder, prefs: ViewConfigs) {
        if (prefs == getDefaultViewConfigPrefs()) {
            runBlocking {
                globalClass.prefDataStore.edit {
                    it.remove(stringPreferencesKey("viewConfigPrefs_${content.uniquePath}"))
                }
            }
        } else {
            runBlocking {
                globalClass.prefDataStore.edit {
                    it[stringPreferencesKey("viewConfigPrefs_${content.uniquePath}")] =
                        prefs.toJson()
                }
            }
        }
    }

    fun setSortingPrefsFor(content: ContentHolder, prefs: FileSortingPrefs) {
        runBlocking {
            globalClass.prefDataStore.edit {
                it[stringPreferencesKey("fileSortingPrefs_${content.uniquePath}")] =
                    prefs.toJson()
            }
        }
    }

    fun deleteSortingPrefsFor(content: ContentHolder) {
        runBlocking {
            globalClass.prefDataStore.edit {
                it.remove(stringPreferencesKey("fileSortingPrefs_${content.uniquePath}"))
            }
        }
    }

    class SingleChoiceDialog {
        var show by mutableStateOf(false)

        var title = emptyString
            private set
        var description = emptyString
            private set
        var choices = mutableListOf<String>()
            private set
        var onSelect: (choice: Int) -> Unit = {}
            private set
        var selectedChoice = -1

        fun dismiss() {
            show = false
            title = emptyString
            description = emptyString
            choices.clear()
            selectedChoice = -1
            onSelect = {}
        }

        fun show(
            title: String,
            description: String,
            choices: List<String>,
            selectedChoice: Int,
            onSelect: (choice: Int) -> Unit
        ) {
            this.title = title
            this.description = description
            this.choices.clear()
            this.choices.addAll(choices)
            this.onSelect = onSelect
            this.selectedChoice = selectedChoice
            show = true
        }
    }
}