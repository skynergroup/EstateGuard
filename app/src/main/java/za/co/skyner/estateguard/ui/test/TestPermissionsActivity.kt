package za.co.skyner.estateguard.ui.test

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import za.co.skyner.estateguard.databinding.ActivityTestPermissionsBinding
import za.co.skyner.estateguard.ui.permissions.PermissionDialogFragment
import za.co.skyner.estateguard.utils.ActivityPermissionHelper
import za.co.skyner.estateguard.utils.PermissionManager

/**
 * Test activity to verify permission flows work correctly
 * This can be used for testing and debugging permission handling
 */
class TestPermissionsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTestPermissionsBinding
    private lateinit var permissionHelper: ActivityPermissionHelper
    private lateinit var permissionManager: PermissionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize permission components
        permissionManager = PermissionManager(this)
        permissionHelper = ActivityPermissionHelper(this)
        permissionHelper.registerPermissionLauncher()
        
        setupClickListeners()
        updatePermissionStatus()
    }
    
    private fun setupClickListeners() {
        binding.buttonRequestCamera.setOnClickListener {
            requestCameraPermission()
        }
        
        binding.buttonRequestLocation.setOnClickListener {
            requestLocationPermission()
        }
        
        binding.buttonRequestAll.setOnClickListener {
            requestAllPermissions()
        }
        
        binding.buttonShowDialog.setOnClickListener {
            showPermissionDialog()
        }
        
        binding.buttonRefreshStatus.setOnClickListener {
            updatePermissionStatus()
        }
    }
    
    private fun requestCameraPermission() {
        permissionHelper.requestCameraPermission { granted ->
            val message = if (granted) "Camera permission granted!" else "Camera permission denied"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            updatePermissionStatus()
        }
    }
    
    private fun requestLocationPermission() {
        permissionHelper.requestLocationPermission { granted ->
            val message = if (granted) "Location permission granted!" else "Location permission denied"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            updatePermissionStatus()
        }
    }
    
    private fun requestAllPermissions() {
        permissionHelper.requestAllRequiredPermissions { granted ->
            val message = if (granted) "All permissions granted!" else "Some permissions were denied"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            updatePermissionStatus()
        }
    }
    
    private fun showPermissionDialog() {
        val dialog = PermissionDialogFragment.newInstance(PermissionDialogFragment.PERMISSION_ALL)
        dialog.setOnPermissionActionListener { granted ->
            if (granted) {
                requestAllPermissions()
            } else {
                Toast.makeText(this, "Permission request cancelled", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show(supportFragmentManager, "permission_dialog")
    }
    
    private fun updatePermissionStatus() {
        val cameraStatus = if (permissionManager.hasCameraPermission()) "✓ Granted" else "✗ Denied"
        val locationStatus = if (permissionManager.hasLocationPermission()) "✓ Granted" else "✗ Denied"
        val allStatus = if (permissionManager.hasAllRequiredPermissions()) "✓ All Granted" else "✗ Missing Permissions"
        
        binding.textCameraStatus.text = "Camera: $cameraStatus"
        binding.textLocationStatus.text = "Location: $locationStatus"
        binding.textAllStatus.text = "Overall: $allStatus"
        
        // Show missing permissions
        val missing = permissionManager.getMissingPermissions()
        if (missing.isNotEmpty()) {
            binding.textMissingPermissions.text = "Missing: ${missing.joinToString(", ")}"
        } else {
            binding.textMissingPermissions.text = "All required permissions granted!"
        }
    }
}
