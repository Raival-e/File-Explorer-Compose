package com.raival.compose.file.explorer.screen.preferences

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.base.BaseActivity
import com.raival.compose.file.explorer.common.compose.SafeSurface
import com.raival.compose.file.explorer.common.compose.Space
import com.raival.compose.file.explorer.screen.preferences.compose.DisplayContainer
import com.raival.compose.file.explorer.screen.preferences.compose.GeneralContainer
import com.raival.compose.file.explorer.screen.preferences.compose.SingleChoiceDialog
import com.raival.compose.file.explorer.screen.preferences.compose.TextEditorContainer
import com.raival.compose.file.explorer.ui.theme.FileExplorerTheme

class PreferencesActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkPermissions()
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onPermissionGranted() {
        setContent {
            FileExplorerTheme {
                SafeSurface(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(color = colorScheme.surfaceContainer)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = null
                            )
                        }
                        Column(
                            Modifier.weight(1f)
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(id = R.string.preferences),
                                fontSize = 21.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    SingleChoiceDialog()

                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())) {
                        Space(size = 4.dp)
                        DisplayContainer()
                        GeneralContainer()
                        TextEditorContainer()
                        Space(size = 4.dp)
                    }
                }
            }
        }
    }
}