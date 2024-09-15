package com.raival.compose.file.explorer.screen.main.tab.regular

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.screen.main.tab.regular.compose.ApkPreviewDialog
import com.raival.compose.file.explorer.screen.main.tab.regular.compose.BookmarksDialog
import com.raival.compose.file.explorer.screen.main.tab.regular.compose.BottomOptionsBar
import com.raival.compose.file.explorer.screen.main.tab.regular.compose.CreateNewFileDialog
import com.raival.compose.file.explorer.screen.main.tab.regular.compose.DeleteConfirmationDialog
import com.raival.compose.file.explorer.screen.main.tab.regular.compose.FileCompressionDialog
import com.raival.compose.file.explorer.screen.main.tab.regular.compose.FileOptionsMenuDialog
import com.raival.compose.file.explorer.screen.main.tab.regular.compose.FilePropertiesDialog
import com.raival.compose.file.explorer.screen.main.tab.regular.compose.FilesList
import com.raival.compose.file.explorer.screen.main.tab.regular.compose.OpenWithAppListDialog
import com.raival.compose.file.explorer.screen.main.tab.regular.compose.PathListRow
import com.raival.compose.file.explorer.screen.main.tab.regular.compose.RenameFileDialog
import com.raival.compose.file.explorer.screen.main.tab.regular.compose.SearchDialog
import com.raival.compose.file.explorer.screen.main.tab.regular.compose.TaskDialog
import com.raival.compose.file.explorer.screen.main.tab.regular.compose.TaskPanel

@Composable
fun ColumnScope.RegularTabContentView(tab: RegularTab) {
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