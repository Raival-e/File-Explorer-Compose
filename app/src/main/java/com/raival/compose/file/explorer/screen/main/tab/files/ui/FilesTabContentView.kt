package com.raival.compose.file.explorer.screen.main.tab.files.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog.ApkPreviewDialog
import com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog.BookmarksDialog
import com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog.CreateNewFileDialog
import com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog.DeleteConfirmationDialog
import com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog.FileCompressionDialog
import com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog.FileOptionsMenuDialog
import com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog.FilePropertiesDialog
import com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog.OpenWithAppListDialog
import com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog.RenameDialog
import com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog.SearchDialog
import com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog.TaskConflictDialog
import com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog.TaskPanel
import com.raival.compose.file.explorer.screen.main.tab.files.ui.dialog.TaskRunningDialog

@Composable
fun ColumnScope.FilesTabContentView(tab: FilesTab) {
    ApkPreviewDialog(tab)
    OpenWithAppListDialog(tab)
    BookmarksDialog(tab)
    SearchDialog(tab)
    TaskPanel(tab)
    DeleteConfirmationDialog(tab)
    CreateNewFileDialog(tab)
    RenameDialog(tab)
    FileCompressionDialog(tab)
    FileOptionsMenuDialog(tab)
    FilePropertiesDialog(tab)
    TaskRunningDialog()
    TaskConflictDialog()
    PathHistoryRow(tab)
    HorizontalDivider(modifier = Modifier, thickness = 1.dp)
    FilesList(tab)
    BottomOptionsBar(tab)
}