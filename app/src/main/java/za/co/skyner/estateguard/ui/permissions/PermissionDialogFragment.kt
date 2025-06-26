package za.co.skyner.estateguard.ui.permissions

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import za.co.skyner.estateguard.R

class PermissionDialogFragment : DialogFragment() {
    
    companion object {
        private const val ARG_PERMISSION_TYPE = "permission_type"
        private const val ARG_IS_RATIONALE = "is_rationale"
        
        const val PERMISSION_CAMERA = "camera"
        const val PERMISSION_LOCATION = "location"
        const val PERMISSION_ALL = "all"
        
        fun newInstance(permissionType: String, isRationale: Boolean = false): PermissionDialogFragment {
            return PermissionDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PERMISSION_TYPE, permissionType)
                    putBoolean(ARG_IS_RATIONALE, isRationale)
                }
            }
        }
    }
    
    private var onPermissionAction: ((Boolean) -> Unit)? = null
    
    fun setOnPermissionActionListener(listener: (Boolean) -> Unit) {
        onPermissionAction = listener
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val permissionType = arguments?.getString(ARG_PERMISSION_TYPE) ?: PERMISSION_ALL
        val isRationale = arguments?.getBoolean(ARG_IS_RATIONALE) ?: false
        
        val (title, message, positiveText) = getDialogContent(permissionType, isRationale)
        
        return AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { _, _ ->
                if (isRationale) {
                    // Open app settings
                    openAppSettings()
                } else {
                    // Request permissions
                    onPermissionAction?.invoke(true)
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                onPermissionAction?.invoke(false)
            }
            .setCancelable(false)
            .create()
    }
    
    private fun getDialogContent(permissionType: String, isRationale: Boolean): Triple<String, String, String> {
        return when (permissionType) {
            PERMISSION_CAMERA -> {
                if (isRationale) {
                    Triple(
                        "Camera Permission Required",
                        "EstateGuard needs camera access to capture incident photos. Please enable camera permission in app settings.",
                        "Open Settings"
                    )
                } else {
                    Triple(
                        "Camera Permission",
                        "EstateGuard needs camera access to capture photos for incident reports. This helps provide visual evidence for security incidents.",
                        "Grant Permission"
                    )
                }
            }
            PERMISSION_LOCATION -> {
                if (isRationale) {
                    Triple(
                        "Location Permission Required",
                        "EstateGuard needs location access to track your position during clock in/out and incident reporting. Please enable location permission in app settings.",
                        "Open Settings"
                    )
                } else {
                    Triple(
                        "Location Permission",
                        "EstateGuard needs location access to:\n• Track your position during clock in/out\n• Record incident locations\n• Ensure accurate time tracking",
                        "Grant Permission"
                    )
                }
            }
            else -> {
                if (isRationale) {
                    Triple(
                        "Permissions Required",
                        "EstateGuard needs camera and location permissions to function properly. Please enable these permissions in app settings.",
                        "Open Settings"
                    )
                } else {
                    Triple(
                        "Permissions Required",
                        "EstateGuard needs the following permissions:\n\n• Camera: To capture incident photos\n• Location: To track your position and record incident locations\n\nThese permissions are essential for security operations.",
                        "Grant Permissions"
                    )
                }
            }
        }
    }
    
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
        startActivity(intent)
    }
}

// Permission status data class
data class PermissionStatus(
    val isGranted: Boolean,
    val shouldShowRationale: Boolean,
    val isPermanentlyDenied: Boolean
)

// Permission result callback
interface PermissionCallback {
    fun onPermissionGranted()
    fun onPermissionDenied(shouldShowRationale: Boolean)
    fun onPermissionPermanentlyDenied()
}

// Permission request result
sealed class PermissionResult {
    object Granted : PermissionResult()
    object Denied : PermissionResult()
    object PermanentlyDenied : PermissionResult()
    data class PartiallyGranted(val grantedPermissions: List<String>, val deniedPermissions: List<String>) : PermissionResult()
}
