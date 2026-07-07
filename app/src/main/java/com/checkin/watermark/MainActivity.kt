package com.checkin.watermark

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import com.checkin.watermark.camera.CameraScreen

class MainActivity : ComponentActivity() {
    private val permissionsGranted = mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        permissionsGranted.value = results[Manifest.permission.CAMERA] == true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
        setContent {
            CameraScreen(
                cameraPermissionGranted = permissionsGranted.value,
                smartLocationEnabled = BuildConfig.SMART_LOCATION,
            )
        }
    }
}
