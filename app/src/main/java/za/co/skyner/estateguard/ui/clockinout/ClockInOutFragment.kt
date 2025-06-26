package za.co.skyner.estateguard.ui.clockinout

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import za.co.skyner.estateguard.auth.FirebaseAuthManager
import za.co.skyner.estateguard.data.repository.EstateGuardRepository
import za.co.skyner.estateguard.data.repository.RepositoryProvider
import za.co.skyner.estateguard.databinding.FragmentClockInOutBinding
import za.co.skyner.estateguard.ui.permissions.PermissionDialogFragment
import za.co.skyner.estateguard.ui.qr.QRScannerActivity
import za.co.skyner.estateguard.utils.FragmentPermissionHelper
import za.co.skyner.estateguard.utils.LocationManager
import za.co.skyner.estateguard.utils.PermissionManager

class ClockInOutFragment : Fragment() {

    private var _binding: FragmentClockInOutBinding? = null
    private val binding get() = _binding!!

    private lateinit var clockInOutViewModel: ClockInOutViewModel
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var repository: EstateGuardRepository
    private lateinit var locationManager: LocationManager
    private lateinit var permissionHelper: FragmentPermissionHelper
    private lateinit var permissionManager: PermissionManager

    // QR Scanner result launcher
    private val qrScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val qrData = result.data?.getStringExtra(QRScannerActivity.EXTRA_QR_RESULT)
            qrData?.let { data ->
                clockInOutViewModel.processQRScan(data)
                Toast.makeText(requireContext(), "QR Code scanned: $data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClockInOutBinding.inflate(inflater, container, false)

        // Initialize dependencies
        authManager = FirebaseAuthManager(requireContext())
        repository = RepositoryProvider.getRepository(requireContext())
        locationManager = LocationManager(requireContext())
        permissionManager = PermissionManager(requireContext())
        permissionHelper = FragmentPermissionHelper(this)

        // Register permission launcher
        permissionHelper.registerPermissionLauncher()

        // Create ViewModel with dependencies
        clockInOutViewModel = ClockInOutViewModelFactory(
            authManager, repository, locationManager
        ).create(ClockInOutViewModel::class.java)

        setupObservers()
        setupClickListeners()
        checkInitialPermissions()

        return binding.root
    }

    private fun setupObservers() {
        // Observe ViewModel data
        clockInOutViewModel.clockStatus.observe(viewLifecycleOwner) { status ->
            binding.textClockStatus.text = status
        }

        clockInOutViewModel.lastActivity.observe(viewLifecycleOwner) { activity ->
            binding.textLastActivity.text = activity
        }

        clockInOutViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonScanQr.isEnabled = !isLoading
            binding.buttonManualEntry.isEnabled = !isLoading
        }

        clockInOutViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupClickListeners() {
        // QR Code scanning
        binding.buttonScanQr.setOnClickListener {
            checkPermissionsAndScanQR()
        }

        // Manual clock in/out
        binding.buttonManualEntry.setOnClickListener {
            checkPermissionsAndManualEntry()
        }
    }

    private fun checkInitialPermissions() {
        if (!permissionManager.hasAllRequiredPermissions()) {
            showPermissionExplanationDialog()
        }
    }

    private fun checkPermissionsAndScanQR() {
        if (permissionManager.hasCameraPermission()) {
            launchQRScanner()
        } else {
            requestCameraPermission { granted ->
                if (granted) {
                    launchQRScanner()
                } else {
                    showPermissionDeniedMessage("Camera permission is required to scan QR codes")
                }
            }
        }
    }

    private fun checkPermissionsAndManualEntry() {
        if (permissionManager.hasLocationPermission()) {
            clockInOutViewModel.manualClockInOut()
        } else {
            requestLocationPermission { granted ->
                if (granted) {
                    clockInOutViewModel.manualClockInOut()
                } else {
                    showPermissionDeniedMessage("Location permission is required for clock in/out")
                }
            }
        }
    }

    private fun launchQRScanner() {
        val intent = Intent(requireContext(), QRScannerActivity::class.java)
        qrScannerLauncher.launch(intent)
    }

    private fun requestCameraPermission(onResult: (Boolean) -> Unit) {
        permissionHelper.requestCameraPermission(onResult)
    }

    private fun requestLocationPermission(onResult: (Boolean) -> Unit) {
        permissionHelper.requestLocationPermission(onResult)
    }

    private fun showPermissionExplanationDialog() {
        val dialog = PermissionDialogFragment.newInstance(PermissionDialogFragment.PERMISSION_ALL)
        dialog.setOnPermissionActionListener { granted ->
            if (granted) {
                permissionHelper.requestAllRequiredPermissions { allGranted ->
                    if (!allGranted) {
                        showPermissionDeniedMessage("Some permissions were denied. Full functionality may not be available.")
                    }
                }
            }
        }
        dialog.show(parentFragmentManager, "permission_dialog")
    }

    private fun showPermissionDeniedMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ViewModel Factory
class ClockInOutViewModelFactory(
    private val authManager: FirebaseAuthManager,
    private val repository: EstateGuardRepository,
    private val locationManager: LocationManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClockInOutViewModel::class.java)) {
            return ClockInOutViewModel(authManager, repository, locationManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
