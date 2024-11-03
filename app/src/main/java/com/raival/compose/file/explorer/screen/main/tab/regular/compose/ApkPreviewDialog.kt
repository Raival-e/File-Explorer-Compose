package com.raival.compose.file.explorer.screen.main.tab.regular.compose

import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.raival.compose.file.explorer.App
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.compose.BottomSheetDialog
import com.raival.compose.file.explorer.common.compose.Space
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.toFormattedSize
import com.raival.compose.file.explorer.screen.main.tab.regular.RegularTab

@Composable
fun ApkPreviewDialog(tab: RegularTab) {
    val apkDialog = tab.apkDialog

    if (tab.apkDialog.showApkDialog && apkDialog.apkFile != null) {
        val packageManager = App.globalClass.packageManager
        val context = LocalContext.current
        val apkFile = apkDialog.apkFile!!

        val apkInfo by remember {
            mutableStateOf(packageManager.getPackageArchiveInfo(apkFile.getPath(), 0))
        }

        var icon by remember {
            mutableStateOf<Drawable?>(null)
        }

        val details = remember {
            mutableStateListOf<Pair<String, String>>()
        }

        var appName by remember {
            mutableStateOf(emptyString)
        }

        LaunchedEffect(Unit) {
            apkInfo?.let {
                it.applicationInfo?.sourceDir = apkFile.getPath()
                it.applicationInfo?.publicSourceDir = apkFile.getPath()

                icon = it.applicationInfo?.loadIcon(packageManager)
                appName = it.applicationInfo?.loadLabel(packageManager).toString()

                details.add(Pair(globalClass.getString(R.string.package_name), it.packageName))
                it.versionName?.let {
                    details.add(Pair(globalClass.getString(R.string.version_name), it))
                }
                details.add(
                    Pair(
                        globalClass.getString(R.string.version_code),
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) it.longVersionCode.toString() else it.versionCode.toString()
                    )
                )
                details.add(
                    Pair(
                        globalClass.getString(R.string.size),
                        apkFile.getFileSize().toFormattedSize()
                    )
                )
            }
        }

        BottomSheetDialog(
            onDismissRequest = { apkDialog.hide() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        model = icon,
                        filterQuality = FilterQuality.Low,
                        error = painterResource(id = R.drawable.apk_file_extension),
                        contentScale = ContentScale.Fit,
                        contentDescription = null
                    )
                }

                Space(size = 8.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = appName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Space(size = 8.dp)

                LazyColumn {
                    items(details, key = { it.first }) {
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Text(text = "${it.first}: ")
                            Text(
                                modifier = Modifier.alpha(0.5f),
                                text = it.second,
                                maxLines = 1
                            )
                        }
                    }
                }

                Space(size = 8.dp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {
                            apkDialog.hide()
                            apkFile.openFile(
                                context = context,
                                anonymous = false,
                                skipSupportedExtensions = true,
                                customMimeType = "application/zip"
                            )
                        }
                    ) {
                        Text(text = stringResource(R.string.explore))
                    }

                    TextButton(
                        onClick = {
                            apkDialog.hide()
                            apkFile.openFile(
                                context = context,
                                anonymous = false,
                                skipSupportedExtensions = true
                            )
                        }
                    ) {
                        Text(text = stringResource(R.string.install))
                    }
                }
            }
        }
    }
}