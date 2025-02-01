package com.raival.compose.file.explorer.screen.main.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.ui.block

@Composable
fun AppInfoDialog() {
    val mainActivityManager = globalClass.mainActivityManager
    val appIcon = globalClass.packageManager.getApplicationIcon(globalClass.packageName)
    val versionName =
        globalClass.packageManager.getPackageInfo(globalClass.packageName, 0).versionName
    val context = LocalContext.current

    if (mainActivityManager.showAppInfoDialog) {
        Dialog(
            onDismissRequest = { mainActivityManager.showAppInfoDialog = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .block()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    modifier = Modifier
                        .size(96.dp)
                        .padding(bottom = 16.dp),
                    model = appIcon,
                    contentDescription = null
                )

                Text(
                    modifier = Modifier.padding(top = 6.dp),
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    modifier = Modifier.padding(bottom = 6.dp),
                    text = "version: $versionName",
                    style = MaterialTheme.typography.bodyMedium
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/Raival-e/File-Explorer-Compose")
                            )
                        )
                    }
                ) {
                    Text(text = stringResource(R.string.github))
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        mainActivityManager.showAppInfoDialog = false
                    }
                ) {
                    Text(text = stringResource(R.string.close))
                }
            }
        }
    }
}