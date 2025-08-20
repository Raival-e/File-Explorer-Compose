package com.raival.compose.file.explorer.screen.main.tab.files.zip

import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.common.isNot
import com.raival.compose.file.explorer.common.removeIf
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import kotlinx.coroutines.runBlocking

class ZipManager {
    private val archiveList = hashMapOf<LocalFileHolder, ZipTree>()


    /**
     * Validates the archive trees by checking if their source files are still valid.
     * Removes any invalid archive trees from the `archiveList`.
     *
     * @return A set of unique paths of the invalid archive trees.
     */
    suspend fun validateArchiveTrees(): Set<String> {
        val invalidTrees =
            archiveList.values.filter { !it.source.isValid() }.map { it.source.uniquePath }

        archiveList.removeIf { localFileHolder, zipTree ->
            runBlocking { !localFileHolder.isValid() }
        }

        return invalidTrees.toSet()
    }

    suspend fun checkForSourceChanges(): Boolean {
        var foundChanges = false
        archiveList.values.forEach { zipTree ->
            if (zipTree.source.lastModified isNot zipTree.timeStamp || zipTree.checkExtractedFiles()
                    .isNotEmpty()
            ) {
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
                        (mainManager.getActiveTab() as? FilesTab)?.openFolder(
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
                (mainManager.getActiveTab() as? FilesTab)?.openFolder(
                    createRootContentHolder()
                ) ?: mainManager.replaceCurrentTabWith(
                    FilesTab(createRootContentHolder())
                )
            }
        }
    }
}