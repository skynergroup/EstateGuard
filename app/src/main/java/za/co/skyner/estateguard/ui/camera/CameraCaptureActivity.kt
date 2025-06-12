package za.co.skyner.estateguard.ui.camera

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import za.co.skyner.estateguard.databinding.ActivityCameraCaptureBinding
import za.co.skyner.estateguard.utils.CameraManager
import za.co.skyner.estateguard.utils.CameraPermissionHelper

class CameraCaptureActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCameraCaptureBinding
    private lateinit var cameraManager: CameraManager
    private lateinit var permissionHelper: CameraPermissionHelper
    
    companion object {
        const val EXTRA_PHOTO_URI = "photo_uri"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupCamera()
        setupClickListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Capture Incident Photo"
        }
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupCamera() {
        cameraManager = CameraManager(this, this)
        permissionHelper = CameraPermissionHelper(this)
        permissionHelper.registerPermissionLauncher()
        
        checkCameraPermission()
    }
    
    private fun checkCameraPermission() {
        permissionHelper.requestCameraPermission { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    
    private fun startCamera() {
        cameraManager.startCamera(binding.previewView)
    }
    
    private fun setupClickListeners() {
        binding.buttonCapture.setOnClickListener {
            capturePhoto()
        }
        
        binding.buttonCancel.setOnClickListener {
            finish()
        }
    }
    
    private fun capturePhoto() {
        binding.buttonCapture.isEnabled = false
        
        cameraManager.takePhoto(
            onImageCaptured = { uri ->
                handlePhotoCaptured(uri)
            },
            onError = { exception ->
                Toast.makeText(this, "Photo capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                binding.buttonCapture.isEnabled = true
            }
        )
    }
    
    private fun handlePhotoCaptured(uri: Uri) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_PHOTO_URI, uri.toString())
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraManager.shutdown()
    }
}
