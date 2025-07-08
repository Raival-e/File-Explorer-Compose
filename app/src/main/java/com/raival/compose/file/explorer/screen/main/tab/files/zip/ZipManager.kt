package com.raival.compose.file.explorer.screen.main.tab.files.zip

import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder

class ZipManager {
    private val archiveList = hashMapOf<LocalFileHolder, ZipTree>()

    fun checkForSourceChanges(): Boolean {
        var foundChanges = false
        archiveList.values.forEach { zipTree ->
            if (zipTree.source.lastModified != zipTree.timeStamp) {
                zipTree.reset()
                foundChanges = true
            }
        }
        return foundChanges
    }

    fun openArchive(archive: LocalFileHolder) {
        val existingTreeKey = archiveList.keys.find { archive.uniquePath == it.uniquePath }

        if (existingTreeKey != null) {
            archiveList[existingTreeKey]?.let { existingTree ->
                if (existingTree.timeStamp == archive.lastModified) {
                    globalClass.mainActivityManager.let { mainManager ->
                        (mainManager.tabs[mainManager.selectedTabIndex] as? FilesTab)?.openFolder(
                            existingTree.createRootContentHolder()
                        ) ?: mainManager.replaceCurrentTabWith(
                            FilesTab(existingTree.createRootContentHolder())
                        )
                        return
                    }
                } else {
                    archiveList.remove(existingTreeKey)
                }
            }
        }

        archiveList[archive] = ZipTree(archive).apply {
            globalClass.mainActivityManager.let { mainManager ->
                (mainManager.tabs[mainManager.selectedTabIndex] as? FilesTab)?.openFolder(
                    createRootContentHolder()
                ) ?: mainManager.replaceCurrentTabWith(
                    FilesTab(createRootContentHolder())
                )
            }
        }
    }
}