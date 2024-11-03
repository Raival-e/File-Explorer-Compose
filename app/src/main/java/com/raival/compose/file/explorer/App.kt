package com.raival.compose.file.explorer

import android.app.Application
import android.content.Context
import android.os.Process
import android.util.Log
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
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.printFullStackTrace
import com.raival.compose.file.explorer.common.extension.toFormattedDate
import com.raival.compose.file.explorer.screen.main.MainActivityManager
import com.raival.compose.file.explorer.screen.main.tab.regular.coil.DocumentFileMapper
import com.raival.compose.file.explorer.screen.main.tab.regular.manager.RegularTabManager
import com.raival.compose.file.explorer.screen.main.tab.regular.modal.DocumentHolder
import com.raival.compose.file.explorer.screen.main.tab.regular.provider.FileProvider
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
import kotlinx.coroutines.launch
import org.eclipse.tm4e.core.registry.IThemeSource
import java.io.File
import kotlin.system.exitProcess

class App : Application(), coil3.SingletonImageLoader.Factory {
    companion object {
        lateinit var appContext: Context

        val globalClass
            get() = appContext as App
    }

    val appFiles: DocumentHolder
        get() = DocumentHolder.fromFile(
            File(globalClass.cacheDir, "files").apply {
                if (!exists()) mkdirs()
            }
        )

    val errorLogFile: DocumentHolder
        get() = "logs.txt".let {
            DocumentHolder.fromFile(File(globalClass.cacheDir, it).apply {
                if (!exists()) createNewFile()
            })
        }

    val recycleBinDir: DocumentHolder
        get() = DocumentHolder.fromFile(File(getExternalFilesDir(null), "bin").apply { mkdirs() })

    private var uid = 0

    val textEditorManager: TextEditorManager by lazy { TextEditorManager().also { setupTextMate() } }
    val mainActivityManager: MainActivityManager by lazy { MainActivityManager().also { setupTabs(it) } }
    val regularTabManager: RegularTabManager by lazy { RegularTabManager() }
    val preferencesManager: PreferencesManager by lazy { PreferencesManager() }
    val viewersManager: ViewersManager by lazy { ViewersManager() }

    override fun onCreate() {
        Thread.setDefaultUncaughtExceptionHandler { _: Thread?, throwable: Throwable? ->
            throwable?.let {
                Log.e("AppCrash", emptyString, it).also { log(throwable) }
            }
            Process.killProcess(Process.myPid())
            exitProcess(2)
        }

        super.onCreate()

        appContext = this
    }

    private fun setupTabs(mainActivityManager: MainActivityManager) {
        mainActivityManager.storageDevices.addAll(FileProvider.getStorageDevices(this))
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

    fun log(throwable: Throwable) {
        log(throwable.printFullStackTrace(), "Error")
    }

    fun log(msg: String, header: String = emptyString) {
        if (!errorLogFile.exists()) return
        if (errorLogFile.isFolder()) return

        if (errorLogFile.getFileSize() > 1024 * 100) {
            errorLogFile.writeText(emptyString)
        }

        errorLogFile.appendText(
            buildString {
                if (header.isNotEmpty()) {
                    append(System.lineSeparator().repeat(2))
                    append("-".repeat(4))
                    append(" $header: ${System.currentTimeMillis().toFormattedDate()} ")
                    append("-".repeat(4))
                    append(System.lineSeparator())
                }
                append(msg)
                append(System.lineSeparator())
            }
        )
    }

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