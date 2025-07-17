package com.raival.compose.file.explorer.screen.main.tab.files.zip

import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.App.Companion.logger
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.toUuid
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.holder.ZipFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.zip.model.ZipNode
import java.io.File
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipTree(
    val source: LocalFileHolder,
) {
    var timeStamp = source.lastModified
    val cleanOnExitDir = LocalFileHolder(
        file = File(globalClass.cleanOnExitDir.file, source.uniquePath.toUuid().toString()).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    )
    val extractedFiles = hashMapOf<String, LocalFileHolder>()
    private val zipEntries = hashMapOf<String, ZipEntry>()
    private val nodes = hashMapOf<String, ZipNode>()

    private val root = ZipNode(
        name = source.displayName,
        path = emptyString,
        isDirectory = true,
        lastModified = 0,
        lastAccessed = 0,
        size = 0
    )

    var isReady = false
        private set

    fun getRelatedNode(extractedFile: LocalFileHolder): ZipNode? {
        if (!extractedFile.uniquePath.startsWith(cleanOnExitDir.uniquePath)) return null
        return findNodeByPath(
            extractedFile.uniquePath.removePrefix(cleanOnExitDir.uniquePath)
                .removePrefix(File.separator)
        )
    }

    fun createExtractionDestinationDirFor(node: ZipNode) =
        if (node.parentPath.isEmpty()) cleanOnExitDir.file else File(
            cleanOnExitDir.file,
            node.parentPath
        )

    fun getExtractionDestinationFile(node: ZipNode): LocalFileHolder? {
        val file = File(createExtractionDestinationDirFor(node), node.name)

        if (!file.exists()) return null

        return LocalFileHolder(file)
    }

    fun getRootNode() = root

    fun createRootContentHolder() = ZipFileHolder(this, root)

    fun findNodeByPath(path: String) = nodes[path]

    fun reset() {
        isReady = false
        timeStamp = source.lastModified
    }

    fun prepare() {
        build()
    }

    private fun build() {
        isReady = false
        zipEntries.clear()

        try {
            val zipFile = ZipFile(source.file)
            val enum: Enumeration<*> = zipFile.entries()
            while (enum.hasMoreElements()) {
                val entry = enum.nextElement() as ZipEntry
                zipEntries.put(entry.name, entry)
            }
        } catch (e: Exception) {
            logger.logError(e)
            globalClass.showMsg(R.string.invalid_zip)
        }

        buildTree()

        isReady = true
    }

    private fun buildTree() {
        nodes.clear()
        nodes.put(emptyString, root.apply { children.clear() })

        zipEntries.forEach { entry ->
            val path = entry.key

            val parts = path.split(File.separator)
            var currentNode = root
            var currentPath = root.path

            for ((i, part) in parts.withIndex()) {
                if (part.isNotEmpty()) {
                    val childNode = currentNode.children.find { it.name == part }

                    currentPath = if (currentPath.isEmpty()) part else "$currentPath/$part"

                    if (childNode == null) {
                        val newNode = ZipNode(
                            name = part,
                            path = currentPath,
                            isDirectory = if (i < parts.lastIndex) true else entry.value.isDirectory,
                            lastModified = entry.value.lastModifiedTime?.toMillis() ?: 0,
                            lastAccessed = entry.value.lastAccessTime?.toMillis() ?: 0,
                            size = entry.value.size
                        )
                        currentNode.children.add(newNode)
                        currentNode = newNode

                        nodes.put(currentPath, newNode)
                    } else {
                        currentNode = childNode
                    }
                }
            }
        }

        zipEntries.clear()
    }

    fun checkExtractedFiles(): ArrayList<LocalFileHolder> {
        val result = arrayListOf<LocalFileHolder>()
        extractedFiles.forEach { item ->
            val file = item.value
            if (file.hasSourceChanged()) {
                result.add(file)
            }
        }
        return result
    }

    fun addExtractedFile(node: ZipNode, file: LocalFileHolder) {
        extractedFiles[node.path] = file
    }
}