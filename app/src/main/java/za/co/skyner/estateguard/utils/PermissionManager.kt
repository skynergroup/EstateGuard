package za.co.skyner.estateguard.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class PermissionManager(private val context: Context) {
    
    companion object {
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        const val LOCATION_FINE_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
        const val LOCATION_COARSE_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
        
        // Permission request codes
        const val REQUEST_CAMERA_PERMISSION = 100
        const val REQUEST_LOCATION_PERMISSION = 101
        const val REQUEST_ALL_PERMISSIONS = 102
    }
    
    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, LOCATION_FINE_PERMISSION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, LOCATION_COARSE_PERMISSION) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasAllRequiredPermissions(): Boolean {
        return hasCameraPermission() && hasLocationPermission()
    }
    
    fun getCameraPermissions(): Array<String> {
        return arrayOf(CAMERA_PERMISSION)
    }
    
    fun getLocationPermissions(): Array<String> {
        return arrayOf(LOCATION_FINE_PERMISSION, LOCATION_COARSE_PERMISSION)
    }
    
    fun getAllRequiredPermissions(): Array<String> {
        return arrayOf(CAMERA_PERMISSION, LOCATION_FINE_PERMISSION, LOCATION_COARSE_PERMISSION)
    }
    
    fun getMissingPermissions(): List<String> {
        val missing = mutableListOf<String>()
        
        if (!hasCameraPermission()) {
            missing.add(CAMERA_PERMISSION)
        }
        
        if (!hasLocationPermission()) {
            missing.addAll(getLocationPermissions())
        }
        
        return missing
    }
}

// Enhanced permission helper for Activities
class ActivityPermissionHelper(private val activity: AppCompatActivity) {
    
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var onPermissionResult: ((Map<String, Boolean>) -> Unit)? = null
    private val permissionManager = PermissionManager(activity)
    
    fun registerPermissionLauncher() {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            onPermissionResult?.invoke(permissions)
        }
    }
    
    fun requestPermissions(
        permissions: Array<String>,
        onResult: (Map<String, Boolean>) -> Unit
    ) {
        onPermissionResult = onResult
        
        val missingPermissions = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isEmpty()) {
            // All permissions already granted
            val result = permissions.associateWith { true }
            onResult(result)
        } else {
            permissionLauncher?.launch(permissions)
        }
    }
    
    fun requestCameraPermission(onResult: (Boolean) -> Unit) {
        requestPermissions(permissionManager.getCameraPermissions()) { results ->
            val granted = results.values.all { it }
            onResult(granted)
        }
    }
    
    fun requestLocationPermission(onResult: (Boolean) -> Unit) {
        requestPermissions(permissionManager.getLocationPermissions()) { results ->
            val granted = results.values.any { it } // At least one location permission granted
            onResult(granted)
        }
    }
    
    fun requestAllRequiredPermissions(onResult: (Boolean) -> Unit) {
        requestPermissions(permissionManager.getAllRequiredPermissions()) { results ->
            val cameraGranted = results[PermissionManager.CAMERA_PERMISSION] == true
            val locationGranted = results[PermissionManager.LOCATION_FINE_PERMISSION] == true || 
                                results[PermissionManager.LOCATION_COARSE_PERMISSION] == true
            onResult(cameraGranted && locationGranted)
        }
    }
}

// Enhanced permission helper for Fragments
class FragmentPermissionHelper(private val fragment: Fragment) {
    
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var onPermissionResult: ((Map<String, Boolean>) -> Unit)? = null
    private val permissionManager = PermissionManager(fragment.requireContext())
    
    fun registerPermissionLauncher() {
        permissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            onPermissionResult?.invoke(permissions)
        }
    }
    
    fun requestPermissions(
        permissions: Array<String>,
        onResult: (Map<String, Boolean>) -> Unit
    ) {
        onPermissionResult = onResult
        
        val missingPermissions = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(fragment.requireContext(), permission) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isEmpty()) {
            // All permissions already granted
            val result = permissions.associateWith { true }
            onResult(result)
        } else {
            permissionLauncher?.launch(permissions)
        }
    }
    
    fun requestCameraPermission(onResult: (Boolean) -> Unit) {
        requestPermissions(permissionManager.getCameraPermissions()) { results ->
            val granted = results.values.all { it }
            onResult(granted)
        }
    }
    
    fun requestLocationPermission(onResult: (Boolean) -> Unit) {
        requestPermissions(permissionManager.getLocationPermissions()) { results ->
            val granted = results.values.any { it } // At least one location permission granted
            onResult(granted)
        }
    }
    
    fun requestAllRequiredPermissions(onResult: (Boolean) -> Unit) {
        requestPermissions(permissionManager.getAllRequiredPermissions()) { results ->
            val cameraGranted = results[PermissionManager.CAMERA_PERMISSION] == true
            val locationGranted = results[PermissionManager.LOCATION_FINE_PERMISSION] == true || 
                                results[PermissionManager.LOCATION_COARSE_PERMISSION] == true
            onResult(cameraGranted && locationGranted)
        }
    }
}
