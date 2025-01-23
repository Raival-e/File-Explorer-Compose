package com.raival.compose.file.explorer.screen.main.compose

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.compose.Space
import com.raival.compose.file.explorer.common.extension.toFormattedSize
import com.raival.compose.file.explorer.screen.main.tab.files.FilesTab
import com.raival.compose.file.explorer.screen.main.tab.files.modal.INTERNAL_STORAGE
import com.raival.compose.file.explorer.screen.main.tab.files.modal.ROOT
import com.raival.compose.file.explorer.screen.main.tab.files.provider.FileProvider
import com.raival.compose.file.explorer.screen.preferences.PreferencesActivity
import kotlinx.coroutines.launch

@Composable
fun DrawerContent() {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val isTablet = configuration.screenWidthDp >= 600
    val drawerWidth = if(isLandscape || isTablet) 400.dp else configuration.screenWidthDp.dp / 4 * 3
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxHeight()
            .width(drawerWidth)
            .background(color = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(color = MaterialTheme.colorScheme.surfaceContainer)
                .padding(8.dp)
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    modifier = Modifier,
                    text = stringResource(id = R.string.app_name),
                    fontSize = 16.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = "https://github.com/raival",
                    fontSize = 12.sp,
                    maxLines = 1,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/Raival-e/File-Explorer-Compose")
                    )
                )
            }) {
                Icon(imageVector = Icons.Rounded.OpenInBrowser, contentDescription = null)
            }

            IconButton(onClick = {
                context.startActivity(Intent(context, PreferencesActivity::class.java))
            }) {
                Icon(imageVector = Icons.Rounded.Settings, contentDescription = null)
            }
        }

        FileProvider.getStorageDevices(globalClass).forEach { storageDevice ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        globalClass.mainActivityManager.apply {
                            coroutineScope.launch {
                                drawerState.close()
                            }
                            addTabAndSelect(FilesTab(storageDevice.documentHolder))
                        }

                    }
                    .padding(12.dp)
                    .padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.padding(8.dp),
                    imageVector = when(storageDevice.type) {
                        INTERNAL_STORAGE -> Icons.Rounded.FolderOpen
                        ROOT -> Icons.Rounded.Numbers
                        else -> Icons.Rounded.SdStorage
                    },
                    contentDescription = null
                )
                Space(size = 8.dp)
                Column {
                    Text(text = storageDevice.title)
                    LinearProgressIndicator(
                        modifier = Modifier.height(8.dp),
                        progress = { storageDevice.usedSize.toFloat() / storageDevice.totalSize },
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        modifier = Modifier
                            .alpha(0.6f),
                        text = "${storageDevice.usedSize.toFormattedSize()} used of ${storageDevice.totalSize.toFormattedSize()}",
                        fontSize = 12.sp,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}
