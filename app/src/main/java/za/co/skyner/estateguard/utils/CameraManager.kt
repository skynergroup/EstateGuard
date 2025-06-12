package za.co.skyner.estateguard.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService
    
    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
    
    init {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    
    fun startCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            
            // Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            
            // ImageCapture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                // Unbind use cases before rebinding
                cameraProvider?.unbindAll()
                
                // Bind use cases to camera
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                
            } catch (exc: Exception) {
                // Handle error
            }
            
        }, ContextCompat.getMainExecutor(context))
    }
    
    fun takePhoto(onImageCaptured: (Uri) -> Unit, onError: (Exception) -> Unit) {
        val imageCapture = imageCapture ?: return
        
        // Create time stamped name and MediaStore entry
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        
        val photoFile = File(
            getOutputDirectory(),
            "IMG_${name}.jpg"
        )
        
        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
                
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    onImageCaptured(savedUri)
                }
            }
        )
    }
    
    private fun getOutputDirectory(): File {
        val mediaDir = File(context.externalMediaDirs.firstOrNull(), "EstateGuard").apply {
            mkdirs()
        }
        return if (mediaDir.exists()) mediaDir else context.filesDir
    }
    
    fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun getRequiredPermissions(): Array<String> {
        return REQUIRED_PERMISSIONS
    }
    
    fun shutdown() {
        cameraExecutor.shutdown()
    }
}

// Result classes for camera operations
sealed class CameraResult {
    data class Success(val imageUri: Uri) : CameraResult()
    data class Error(val exception: Exception) : CameraResult()
}

// Permission helper
class CameraPermissionHelper(private val activity: AppCompatActivity) {
    
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var onPermissionResult: ((Boolean) -> Unit)? = null
    
    fun registerPermissionLauncher() {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            onPermissionResult?.invoke(allGranted)
        }
    }
    
    fun requestCameraPermission(onResult: (Boolean) -> Unit) {
        onPermissionResult = onResult
        
        if (hasCameraPermission()) {
            onResult(true)
        } else {
            permissionLauncher?.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }
    
    private fun hasCameraPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}
