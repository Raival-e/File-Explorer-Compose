package com.raival.compose.file.explorer.screen.main.tab.files

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.screen.main.tab.files.compose.ApkPreviewDialog
import com.raival.compose.file.explorer.screen.main.tab.files.compose.BookmarksDialog
import com.raival.compose.file.explorer.screen.main.tab.files.compose.BottomOptionsBar
import com.raival.compose.file.explorer.screen.main.tab.files.compose.CreateNewFileDialog
import com.raival.compose.file.explorer.screen.main.tab.files.compose.DeleteConfirmationDialog
import com.raival.compose.file.explorer.screen.main.tab.files.compose.FileCompressionDialog
import com.raival.compose.file.explorer.screen.main.tab.files.compose.FileOptionsMenuDialog
import com.raival.compose.file.explorer.screen.main.tab.files.compose.FilePropertiesDialog
import com.raival.compose.file.explorer.screen.main.tab.files.compose.FilesList
import com.raival.compose.file.explorer.screen.main.tab.files.compose.OpenWithAppListDialog
import com.raival.compose.file.explorer.screen.main.tab.files.compose.PathListRow
import com.raival.compose.file.explorer.screen.main.tab.files.compose.RenameFileDialog
import com.raival.compose.file.explorer.screen.main.tab.files.compose.SearchDialog
import com.raival.compose.file.explorer.screen.main.tab.files.compose.TaskDialog
import com.raival.compose.file.explorer.screen.main.tab.files.compose.TaskPanel

@Composable
fun ColumnScope.RegularTabContentView(tab: FilesTab) {
    BackHandler(enabled = tab.handleBackGesture) { tab.onBackPressed() }
    ApkPreviewDialog(tab)
    OpenWithAppListDialog(tab)
    BookmarksDialog(tab)
    SearchDialog(tab)
    TaskPanel(tab)
    TaskDialog(tab)
    RenameFileDialog(tab)
    DeleteConfirmationDialog(tab)
    CreateNewFileDialog(tab)
    FileCompressionDialog(tab)
    FileOptionsMenuDialog(tab)
    FilePropertiesDialog(tab)
    PathListRow(tab)
    HorizontalDivider(modifier = Modifier, thickness = 1.dp)
    FilesList(tab)
    BottomOptionsBar(tab)
}