package com.checkin.watermark.camera

import android.content.Context
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.checkin.watermark.domain.WatermarkTemplate
import com.checkin.watermark.domain.WatermarkTextBuilder
import com.checkin.watermark.location.DeviceLocationReader
import com.checkin.watermark.location.manual.ManualLocationProvider
import com.checkin.watermark.location.smart.InMemoryAddressCache
import com.checkin.watermark.location.smart.ReverseGeocoder
import com.checkin.watermark.location.smart.SmartLocationResolver
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
    val imageCapture = remember { ImageCapture.Builder().build() }
    var manualLocationName by remember { mutableStateOf("点击水印编辑地点") }
    var editingLocation by remember { mutableStateOf(false) }
    var watermarkLines by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(smartLocationEnabled, manualLocationName) {
        val fix = DeviceLocationReader(context).readLastKnownLocation()
        val location = resolvePreviewLocation(
            smartLocationEnabled = smartLocationEnabled,
            coordinate = fix?.coordinate,
            accuracyMeters = fix?.accuracyMeters,
            manualLocationName = manualLocationName,
        )
        watermarkLines = WatermarkTextBuilder(ZoneId.systemDefault()).build(
            template = WatermarkTemplate.WorkCheckin,
            capturedAt = Instant.now(),
            location = location,
        )
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PreviewView(viewContext).also { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(viewContext)
                    cameraProviderFuture.addListener(
                        {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture,
                            )
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
                .padding(start = 16.dp, end = 16.dp, bottom = 120.dp),
            onClick = if (smartLocationEnabled) null else ({ editingLocation = true }),
        )

        Button(
            onClick = { capturePhoto(context, imageCapture, watermarkLines) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .size(76.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.72f)),
        ) {
            Text("")
        }
    }

    if (editingLocation) {
        ManualLocationDialog(
            value = manualLocationName,
            onValueChange = { manualLocationName = it },
            onDismiss = { editingLocation = false },
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
) {
    val outputFile = File(context.cacheDir, "checkin-${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val saved = PhotoSaver(context).renderWatermarkAndSaveToGallery(outputFile, watermarkLines)
                val message = if (saved) "水印照片已保存到相册" else "照片保存失败"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, "拍照失败：${exception.message}", Toast.LENGTH_SHORT).show()
            }
        },
    )
}

@Composable
private fun ManualLocationDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑地点") },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text("水印地点") },
                singleLine = false,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("保存")
            }
        },
    )
}
