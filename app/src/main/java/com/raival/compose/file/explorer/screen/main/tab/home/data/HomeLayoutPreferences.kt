package com.raival.compose.file.explorer.screen.main.tab.home.data

import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import kotlinx.serialization.Serializable

val defaultHomeTabSections = HomeLayout(
    listOf(
        HomeSectionConfig(
            id = "recent_files",
            type = HomeSectionType.RECENT_FILES,
            title = globalClass.getString(R.string.recent_files),
            isEnabled = true,
            order = 0
        ),
        HomeSectionConfig(
            id = "categories",
            type = HomeSectionType.CATEGORIES,
            title = globalClass.getString(R.string.categories),
            isEnabled = true,
            order = 1
        ),
        HomeSectionConfig(
            id = "storage",
            type = HomeSectionType.STORAGE,
            title = globalClass.getString(R.string.storage),
            isEnabled = true,
            order = 2
        ),
        HomeSectionConfig(
            id = "bookmarks",
            type = HomeSectionType.BOOKMARKS,
            title = globalClass.getString(R.string.bookmarks),
            isEnabled = true,
            order = 3
        ),
        HomeSectionConfig(
            id = "recycle_bin",
            type = HomeSectionType.RECYCLE_BIN,
            title = globalClass.getString(R.string.recycle_bin),
            isEnabled = true,
            order = 4
        ),
        HomeSectionConfig(
            id = "jump_to_path",
            type = HomeSectionType.JUMP_TO_PATH,
            title = globalClass.getString(R.string.jump_to_path),
            isEnabled = true,
            order = 5
        )
    )
)

@Serializable
data class HomeLayout(
    val sections: List<HomeSectionConfig>
)

@Serializable
data class HomeSectionConfig(
    val id: String,
    val type: HomeSectionType,
    val title: String,
    val isEnabled: Boolean,
    var order: Int
)

@Serializable
enum class HomeSectionType {
    RECENT_FILES,
    CATEGORIES,
    STORAGE,
    BOOKMARKS,
    RECYCLE_BIN,
    JUMP_TO_PATH
}