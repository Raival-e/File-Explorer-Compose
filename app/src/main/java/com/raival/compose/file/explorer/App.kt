package com.raival.compose.file.explorer

import android.app.Application
import android.content.Context
import android.os.Environment
import android.os.Process
import android.widget.Toast
import androidx.annotation.StringRes
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.bitmapFactoryMaxParallelism
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.addLastModifiedToFileCacheKey
import coil3.request.allowConversionToBitmap
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import coil3.video.VideoFrameDecoder
import com.raival.compose.file.explorer.coil.apk.ApkFileDecoder
import com.raival.compose.file.explorer.coil.pdf.PdfFileDecoder
import com.raival.compose.file.explorer.common.logger.FileExplorerLogger
import com.raival.compose.file.explorer.screen.main.MainActivityManager
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTabManager
import com.raival.compose.file.explorer.screen.main.tab.files.coil.DocumentFileMapper
import com.raival.compose.file.explorer.screen.main.tab.files.holder.LocalFileHolder
import com.raival.compose.file.explorer.screen.main.tab.files.task.TaskManager
import com.raival.compose.file.explorer.screen.main.tab.files.zip.ZipManager
import com.raival.compose.file.explorer.screen.preferences.PreferencesManager
import com.raival.compose.file.explorer.screen.textEditor.TextEditorManager
import com.raival.compose.file.explorer.screen.viewer.ViewersManager
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.eclipse.tm4e.core.registry.IThemeSource
import java.io.File
import kotlin.system.exitProcess

class App : Application(), coil3.SingletonImageLoader.Factory {
    companion object {
        lateinit var appContext: Context

        val globalClass
            get() = appContext as App

        val logger
            get() = globalClass.logger
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var logger: FileExplorerLogger
        private set

    val appFiles: LocalFileHolder
        get() = LocalFileHolder(
            File(globalClass.cacheDir, "files").apply { if (!exists()) mkdirs() }
        )

    val recycleBinDir: LocalFileHolder
        get() = LocalFileHolder(
            File(
                Environment.getExternalStorageDirectory(),
                ".prism/bin"
            ).apply { mkdirs() })

    private var uid = 0

    val textEditorManager: TextEditorManager by lazy { TextEditorManager().also { setupTextMate() } }
    val mainActivityManager: MainActivityManager by lazy { MainActivityManager().also { it.setupTabs() } }
    val filesTabManager: FilesTabManager by lazy { FilesTabManager() }
    val preferencesManager: PreferencesManager by lazy { PreferencesManager() }
    val viewersManager: ViewersManager by lazy {
        setupTextMate()
        ViewersManager()
    }
    val taskManager: TaskManager by lazy { TaskManager() }
    val zipManager: ZipManager by lazy { ZipManager() }

    override fun onCreate() {
        super.onCreate()

        logger = FileExplorerLogger(this, applicationScope)
        setupGlobalExceptionHandler()

        appContext = this
    }

    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            logger.logError(exception)
            defaultHandler?.uncaughtException(thread, exception)
            Process.killProcess(Process.myPid())
            exitProcess(2)
        }
    }

    private fun setupTextMate() {
        CoroutineScope(Dispatchers.IO).launch {
            FileProviderRegistry.getInstance().addFileProvider(
                AssetsFileResolver(
                    appContext.assets
                )
            )

            GrammarRegistry.getInstance().loadGrammars("textmate/languages.json")

            val themeRegistry = ThemeRegistry.getInstance()
            themeRegistry.loadTheme(
                ThemeModel(
                    IThemeSource.fromInputStream(
                        FileProviderRegistry
                            .getInstance()
                            .tryGetInputStream("textmate/dark.json"),
                        "dark.json", null
                    ),
                    "dark"
                )
            )

            themeRegistry.loadTheme(
                ThemeModel(
                    IThemeSource.fromInputStream(
                        FileProviderRegistry
                            .getInstance()
                            .tryGetInputStream("textmate/light.tmTheme"),
                        "light.tmTheme",
                        null
                    ),
                    "light"
                )
            )
        }
    }

    fun showMsg(@StringRes msgSrc: Int) {
        showMsg(getString(msgSrc))
    }

    fun showMsg(msg: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@App, msg, Toast.LENGTH_SHORT).show()
        }
    }


    fun generateUid() = uid++

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader(context)
            .newBuilder()
            .addLastModifiedToFileCacheKey(true)
            .crossfade(true)
            .allowConversionToBitmap(true)
            .coroutineContext(Dispatchers.Default)
            .interceptorCoroutineContext(Dispatchers.Default)
            .bitmapFactoryMaxParallelism(1)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.35)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .components {
                add(DocumentFileMapper())
                add(GifDecoder.Factory())
                add(SvgDecoder.Factory())
                add(VideoFrameDecoder.Factory())
                add(ApkFileDecoder.Factory())
                add(PdfFileDecoder.Factory())
            }
            .build()
    }
}