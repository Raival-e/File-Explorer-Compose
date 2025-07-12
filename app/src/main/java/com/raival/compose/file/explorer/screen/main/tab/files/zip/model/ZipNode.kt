package com.raival.compose.file.explorer.screen.main.tab.files.zip.model

import com.raival.compose.file.explorer.common.extension.emptyString
import java.io.File

data class ZipNode(
    val name: String,
    val path: String,
    val size: Long = 0L,
    val isDirectory: Boolean = true,
    val lastModified: Long = 0L,
    val lastAccessed: Long = 0L,
    val children: MutableList<ZipNode> = mutableListOf()
) {
    val extension: String
        get() {
            if (this.isDirectory) {
                return emptyString
            }

            val dotIndex = this.name.lastIndexOf('.')

            // If a dot exists and it is not the first character in the name,
            // return the substring after the dot.
            return if (dotIndex > 0) {
                this.name.substring(dotIndex + 1).lowercase()
            } else {
                // No extension found (or it's a dotfile like ".gitignore").
                emptyString
            }
        }

    val parentPath: String
        get() {
            // First, remove any trailing slash if the entry is a directory.
            // This ensures that for "folder/", we are working with "folder".
            val path = this.path.removeSuffix(File.separator)

            // Find the last occurrence of the path separator.
            val lastSlashIndex = path.lastIndexOf(File.separatorChar)

            // If no slash is found, the entry is in the root.
            // Otherwise, the parent path is everything up to and including the slash.
            return if (lastSlashIndex == -1) {
                emptyString
            } else {
                path.substring(0, lastSlashIndex)
            }
        }

    fun listFilesAndEmptyDirs(): List<ZipNode> {
        val result = mutableListOf<ZipNode>()

        // A queue of directories to visit. Start with the root.
        val directoryQueue = ArrayDeque<ZipNode>()
        if (isDirectory) {
            directoryQueue.add(this)
        } else {
            // If the starting path is just a file, add it and return.
            result.add(this)
            return result
        }

        while (directoryQueue.isNotEmpty()) {
            val dir = directoryQueue.removeFirst()
            // listFiles() can be null if the directory is not accessible
            val children = dir.children

            if (children.isEmpty()) {
                // This directory is empty, add it to the list.
                result.add(dir)
            } else {
                // Process the contents of the non-empty directory
                for (child in children) {
                    if (child.isDirectory) {
                        directoryQueue.add(child)
                    } else {
                        result.add(child)
                    }
                }
            }
        }

        return result
    }
}