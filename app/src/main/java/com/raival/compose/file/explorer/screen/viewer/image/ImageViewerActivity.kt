package com.raival.compose.file.explorer.screen.viewer.image

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.BorderOuter
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WidthFull
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil3.Image
import coil3.asDrawable
import coil3.compose.AsyncImage
import coil3.toBitmap
import com.anggrayudi.storage.extension.toDocumentFile
import com.raival.compose.file.explorer.App.Companion.globalClass
import com.raival.compose.file.explorer.R
import com.raival.compose.file.explorer.common.extension.emptyString
import com.raival.compose.file.explorer.common.extension.read
import com.raival.compose.file.explorer.common.extension.showMsg
import com.raival.compose.file.explorer.common.extension.toFormattedDate
import com.raival.compose.file.explorer.common.extension.toFormattedSize
import com.raival.compose.file.explorer.common.ui.SafeSurface
import com.raival.compose.file.explorer.screen.viewer.ViewerActivity
import com.raival.compose.file.explorer.screen.viewer.ViewerInstance
import com.raival.compose.file.explorer.theme.FileExplorerTheme
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

class ImageViewerActivity : ViewerActivity() {
    override fun onCreateNewInstance(uri: Uri, uid: String): ViewerInstance {
        return ImageViewerInstance(uri, uid)
    }

    override fun onReady(instance: ViewerInstance) {
        setContent {
            FileExplorerTheme {
                SafeSurface(enableStatusBarsPadding = false) {
                    ImageViewerScreen(instance)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageViewerScreen(instance: ViewerInstance) {
    val defaultColor = MaterialTheme.colorScheme.surface
    var dominantColor by remember { mutableStateOf(defaultColor) }
    var secondaryColor by remember { mutableStateOf(defaultColor) }
    val imageBackgroundColors = arrayListOf(
        Color.Transparent,
        Color.White,
        Color.Gray,
        Color.Black
    )
    var currentImageBackgroundColorIndex by remember { mutableIntStateOf(0) }
    var imageData by remember { mutableStateOf(ByteArray(0)) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var showInfo by remember { mutableStateOf(false) }
    var imageInfo by remember { mutableStateOf<ImageInfo?>(null) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var imageDimensions by remember { mutableStateOf("" to "") }
    var contentScale by remember { mutableStateOf(ContentScale.Fit) }
    val context = LocalContext.current
    val zoomState = rememberZoomState()
    val scope = rememberCoroutineScope()

    // Load image data
    LaunchedEffect(instance.uri) {
        try {
            imageData = instance.uri.read()
            isLoading = false
        } catch (_: Exception) {
            isError = true
            isLoading = false
        }
    }

    // Extract image info when image is loaded
    LaunchedEffect(imageData, imageDimensions.first) {
        if (imageData.isNotEmpty() && imageDimensions.first.isNotEmpty()) {
            imageInfo =
                extractImageInfo(instance.uri, imageDimensions.first, imageDimensions.second)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        dominantColor.copy(alpha = 0.8f),
                        secondaryColor.copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        when {
            isLoading -> LoadingState()
            isError -> ErrorState(onRetry = {
                scope.launch {
                    try {
                        imageData = instance.uri.read()
                        isLoading = false
                    } catch (_: Exception) {
                        isError = true
                        isLoading = false
                    }
                }
            })

            else -> {
                // Main image with zoom and rotation
                var image by remember { mutableStateOf<Image?>(null) }

                AsyncImage(
                    model = imageData,
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = rotationAngle }
                        .zoomable(
                            zoomState = zoomState,
                            onTap = { showControls = !showControls },
                        )
                        .background(imageBackgroundColors[currentImageBackgroundColorIndex]),
                    onSuccess = { state ->
                        image = state.result.image
                        val drawable = state.result.image.asDrawable(context.resources)
                        imageDimensions =
                            "${drawable.intrinsicWidth}" to "${drawable.intrinsicHeight}"
                    }
                )

                // Extract dominant color
                LaunchedEffect(image) {
                    if (image != null) {
                        val bitmap = image!!.toBitmap().copy(Bitmap.Config.ARGB_8888, false)
                        val palette = Palette.from(bitmap).generate()
                        dominantColor = Color(palette.getDominantColor(defaultColor.toArgb()))
                        secondaryColor = Color(palette.getMutedColor(defaultColor.toArgb()))
                    }
                }

                // Animated gradient overlay for controls
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
                    exit = fadeOut(spring(stiffness = Spring.StiffnessMedium))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        defaultColor.copy(alpha = 0.9f),
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Transparent,
                                        defaultColor.copy(alpha = 0.9f)
                                    )
                                )
                            )
                    )
                }

                // Top bar
                AnimatedVisibility(
                    visible = showControls,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut()
                ) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = imageInfo?.name ?: stringResource(R.string.unknown),
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                imageInfo?.let { info ->
                                    Text(
                                        text = "${info.size} • ${info.dimensions}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                (context as? ViewerActivity)?.finish()
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { showInfo = true }) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }

                // Bottom controls
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    visible = showControls,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    BottomControls(
                        onInvertBackgroundColors = {
                            currentImageBackgroundColorIndex =
                                (currentImageBackgroundColorIndex + 1) % imageBackgroundColors.size
                        },
                        onRotate = {
                            rotationAngle = (rotationAngle + 90f) % 360f
                        },
                        onEdit = {
                            val editIntent = Intent(Intent.ACTION_EDIT)
                            val mimeType =
                                context.contentResolver.getType(instance.uri) ?: "image/*"
                            editIntent.setDataAndType(instance.uri, mimeType)
                            editIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                            val chooser = Intent.createChooser(
                                editIntent,
                                globalClass.getString(R.string.edit_with)
                            )
                            if (editIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(chooser)
                            } else {
                                showMsg(context.getString(R.string.no_app_found_to_edit_this_image))
                            }
                        },
                        onContentScale = {
                            contentScale = it
                        },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }

                // Info bottom sheet
                if (showInfo) {
                    imageInfo?.let { info ->
                        ImageInfoBottomSheet(
                            imageInfo = info,
                            onDismiss = { showInfo = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.loading_image),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.failed_to_load_image),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.check_file_exists_and_try_again),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun BottomControls(
    onInvertBackgroundColors: () -> Unit,
    onRotate: () -> Unit,
    onEdit: () -> Unit,
    onContentScale: (contentScale: ContentScale) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentScales = arrayListOf<ContentScale>().apply {
        add(ContentScale.Fit)
        add(ContentScale.Crop)
        add(ContentScale.FillWidth)
        add(ContentScale.FillHeight)
        add(ContentScale.FillBounds)
        add(ContentScale.Inside)
    }
    var selectedContentScale by remember { mutableStateOf(contentScales[0]) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Invert background colors
        ActionButton(
            icon = Icons.Default.InvertColors,
            onClick = onInvertBackgroundColors,
            backgroundColor = Color.White.copy(alpha = 0.15f)
        )

        // Fit to screen
        ActionButton(
            icon = when (selectedContentScale) {
                ContentScale.Fit -> Icons.Default.FitScreen
                ContentScale.Crop -> Icons.Default.Crop
                ContentScale.FillWidth -> Icons.Default.WidthFull
                ContentScale.FillHeight -> Icons.Default.Height
                ContentScale.FillBounds -> Icons.Default.BorderOuter
                else -> Icons.Default.FilterCenterFocus
            },
            onClick = {
                val currentScaleIndex = contentScales.indexOf(selectedContentScale)
                selectedContentScale = contentScales[
                    if (currentScaleIndex == contentScales.lastIndex) 0
                    else currentScaleIndex + 1
                ]
                onContentScale(selectedContentScale)
            },
            backgroundColor = Color.White.copy(alpha = 0.15f)
        )

        // Rotate
        ActionButton(
            icon = Icons.AutoMirrored.Filled.RotateRight,
            onClick = onRotate,
            backgroundColor = Color.White.copy(alpha = 0.15f)
        )

        // Edit
        ActionButton(
            icon = Icons.Default.Edit,
            onClick = onEdit,
            backgroundColor = Color.White.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color = Color.White,
    backgroundColor: Color = Color.White.copy(alpha = 0.1f)
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageInfoBottomSheet(
    imageInfo: ImageInfo,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.size(width = 32.dp, height = 4.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Image Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            InfoRow(stringResource(R.string.name), imageInfo.name)
            InfoRow(stringResource(R.string.size), imageInfo.size)
            InfoRow(stringResource(R.string.dimensions), imageInfo.dimensions)
            InfoRow(stringResource(R.string.format), imageInfo.format)
            InfoRow(stringResource(R.string.last_modified), imageInfo.lastModified)
            InfoRow(stringResource(R.string.path), imageInfo.path)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(2f)
        )
    }
}

// Data class for image information
data class ImageInfo(
    val name: String,
    val size: String,
    val dimensions: String,
    val format: String,
    val lastModified: String,
    val path: String
)

// Helper function to extract image information
private fun extractImageInfo(uri: Uri, width: String, height: String): ImageInfo {
    val file = uri.toDocumentFile(globalClass)

    return ImageInfo(
        name = file?.name ?: emptyString,
        size = (file?.length() ?: 0).toFormattedSize(),
        dimensions = if (width.isNotEmpty() && height.isNotEmpty()) "$width × $height" else globalClass.getString(
            R.string.unknown
        ),
        format = globalClass.contentResolver.getType(uri)
            ?.substringAfter("image/", globalClass.getString(R.string.not_available))
            ?.uppercase()
            ?: globalClass.getString(R.string.not_available),
        lastModified = (file?.lastModified() ?: 0).toFormattedDate(),
        path = uri.path.toString()
    )
}