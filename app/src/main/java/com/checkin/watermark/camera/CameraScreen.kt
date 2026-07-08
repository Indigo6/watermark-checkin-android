package com.checkin.watermark.camera

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.checkin.watermark.domain.Coordinate
import com.checkin.watermark.domain.LocationSnapshot
import com.checkin.watermark.domain.LocationSource
import com.checkin.watermark.domain.WatermarkTemplate
import com.checkin.watermark.domain.WatermarkTextBuilder
import com.checkin.watermark.location.DeviceLocationReader
import com.checkin.watermark.location.ManualLocationStore
import com.checkin.watermark.location.manual.ManualLocationProvider
import com.checkin.watermark.location.manual.ManualWorkLocationBook
import com.checkin.watermark.location.smart.InMemoryAddressCache
import com.checkin.watermark.location.smart.ReverseGeocoder
import com.checkin.watermark.location.smart.SmartLocationResolver
import com.checkin.watermark.record.CaptureRecordStore
import com.checkin.watermark.watermark.WatermarkOverlay
import java.io.File
import java.time.Instant
import java.time.ZoneId

@Composable
fun CameraScreen(
    cameraPermissionGranted: Boolean,
    smartLocationEnabled: Boolean,
) {
    if (!cameraPermissionGranted) {
        PermissionRequired()
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember {
        ImageCapture.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()
    }
    val manualLocationStore = remember { ManualLocationStore(context) }
    var manualLocationBook by remember {
        mutableStateOf(
            manualLocationStore.readBook().let { book ->
                if (book.locations.isEmpty()) book.add("点击水印添加地点") else book
            },
        )
    }
    var editingLocation by remember { mutableStateOf(false) }
    var watermarkLines by remember { mutableStateOf(emptyList<String>()) }
    var currentLocation by remember {
        mutableStateOf(
            LocationSnapshot(
                displayName = "定位中",
                coordinate = null,
                accuracyMeters = null,
                source = LocationSource.Unavailable,
            ),
        )
    }
    var lastSavedUri by remember { mutableStateOf<Uri?>(null) }

    val manualLocationName = manualLocationBook.selectedLocation()?.name ?: "点击水印添加地点"

    LaunchedEffect(smartLocationEnabled, manualLocationName) {
        val reader = DeviceLocationReader(context)
        val fix = reader.readLastKnownLocation()
        currentLocation = resolvePreviewLocation(
            smartLocationEnabled = smartLocationEnabled,
            coordinate = fix?.coordinate,
            accuracyMeters = fix?.accuracyMeters,
            manualLocationName = manualLocationName,
        )
    }

    DisposableEffect(smartLocationEnabled, manualLocationName) {
        val subscription = DeviceLocationReader(context).startLocationUpdates { fix ->
            currentLocation = resolvePreviewLocation(
                smartLocationEnabled = smartLocationEnabled,
                coordinate = fix.coordinate,
                accuracyMeters = fix.accuracyMeters,
                manualLocationName = manualLocationName,
            )
        }
        onDispose { subscription.stop() }
    }

    LaunchedEffect(currentLocation) {
        watermarkLines = WatermarkTextBuilder(ZoneId.systemDefault()).build(
            template = WatermarkTemplate.EvidenceCheckin,
            capturedAt = Instant.now(),
            location = currentLocation,
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF111827)),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .background(Color.Black),
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    PreviewView(viewContext).also { previewView ->
                        previewView.scaleType = PreviewView.ScaleType.FIT_CENTER
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(viewContext)
                        cameraProviderFuture.addListener(
                            {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder()
                                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                                    .build()
                                    .also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                cameraProvider.unbindAll()
                                previewView.post {
                                    val viewPort = previewView.viewPort
                                    if (viewPort == null) {
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageCapture,
                                        )
                                    } else {
                                        val useCaseGroup = UseCaseGroup.Builder()
                                            .setViewPort(viewPort)
                                            .addUseCase(preview)
                                            .addUseCase(imageCapture)
                                            .build()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            useCaseGroup,
                                        )
                                    }
                                }
                            },
                            ContextCompat.getMainExecutor(viewContext),
                        )
                    }
                },
            )

            WatermarkOverlay(
                lines = watermarkLines,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 14.dp, end = 14.dp, bottom = 24.dp),
                onClick = if (smartLocationEnabled) null else ({ editingLocation = true }),
            )
        }

        Button(
            onClick = {
                capturePhoto(context, imageCapture, watermarkLines, currentLocation) { uri ->
                    lastSavedUri = uri
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .size(76.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.72f)),
        ) {
            Text("")
        }

        lastSavedUri?.let { uri ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 36.dp)
                    .size(width = 86.dp, height = 72.dp)
                    .clickable { sharePhoto(context, uri) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(width = 46.dp, height = 34.dp)
                                .background(Color(0xFFE7F6F4)),
                        )
                        Text("分享", color = Color(0xFF0F766E))
                    }
                }
            }
        }
    }

    if (editingLocation) {
        ManualLocationDialog(
            book = manualLocationBook,
            onBookChange = {
                manualLocationBook = it
                manualLocationStore.saveBook(it)
            },
            onDismiss = {
                manualLocationStore.saveBook(manualLocationBook)
                editingLocation = false
            },
        )
    }
}

@Composable
private fun PermissionRequired() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101820)),
        contentAlignment = Alignment.Center,
    ) {
        Text("需要相机权限才能拍照", color = Color.White)
    }
}

private fun resolvePreviewLocation(
    smartLocationEnabled: Boolean,
    coordinate: Coordinate?,
    accuracyMeters: Float?,
    manualLocationName: String,
): LocationSnapshot {
    return if (smartLocationEnabled) {
        SmartLocationResolver(
            sites = emptyList(),
            cache = InMemoryAddressCache(emptyList()),
            backend = object : ReverseGeocoder {
                override fun reverseGeocode(coordinate: Coordinate): String? = null
            },
        ).resolve(coordinate, accuracyMeters = accuracyMeters)
    } else {
        ManualLocationProvider(manualLocationName).resolve(coordinate, accuracyMeters = accuracyMeters)
    }
}

private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    watermarkLines: List<String>,
    location: LocationSnapshot,
    onSaved: (Uri) -> Unit,
) {
    val outputFile = File(context.cacheDir, "checkin-${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val saved = PhotoSaver(context).renderWatermarkAndSaveToGallery(outputFile, watermarkLines, location)
                saved?.record?.let { CaptureRecordStore(context).append(it) }
                saved?.uri?.let(onSaved)
                val message = if (saved != null) "水印照片已保存到相册" else "照片保存失败"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, "拍照失败：${exception.message}", Toast.LENGTH_SHORT).show()
            }
        },
    )
}

private fun sharePhoto(
    context: Context,
    uri: Uri,
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "分享水印照片"))
}

@Composable
private fun ManualLocationDialog(
    book: ManualWorkLocationBook,
    onBookChange: (ManualWorkLocationBook) -> Unit,
    onDismiss: () -> Unit,
) {
    var newLocationName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("工作地点") },
        text = {
            Column {
                book.locations.forEach { location ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable { onBookChange(book.select(location.id)) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (book.selectedLocation()?.id == location.id) {
                                Color(0xFFE7F6F4)
                            } else {
                                Color(0xFFF8FAFC)
                            },
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = location.name,
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF17212B),
                            )
                            TextButton(onClick = { onBookChange(book.select(location.id).deleteSelected()) }) {
                                Text("删除")
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = newLocationName,
                    onValueChange = { newLocationName = it },
                    label = { Text("新增工作地点") },
                    singleLine = true,
                    modifier = Modifier.widthIn(min = 260.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newLocationName.isNotBlank()) {
                        onBookChange(book.add(newLocationName).selectByName(newLocationName))
                    }
                    onDismiss()
                },
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
