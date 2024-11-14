package com.raival.compose.file.explorer.screen.preferences

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.raival.compose.file.explorer.base.BaseActivity
import com.raival.compose.file.explorer.common.compose.Space
import com.raival.compose.file.explorer.screen.preferences.compose.DisplayContainer
import com.raival.compose.file.explorer.screen.preferences.compose.GeneralContainer
import com.raival.compose.file.explorer.screen.preferences.compose.SingleChoiceDialog
import com.raival.compose.file.explorer.screen.preferences.compose.TextEditorContainer
import com.raival.compose.file.explorer.screen.preferences.compose.ToolbarView
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
                Scaffold(
                    topBar = { ToolbarView() }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = colorScheme.surfaceContainerLowest
                    ) {
                        SingleChoiceDialog()
                        Column(
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(
                                    top = paddingValues.calculateTopPadding(),
                                    bottom = paddingValues.calculateBottomPadding()
                                )
                        ) {
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
}