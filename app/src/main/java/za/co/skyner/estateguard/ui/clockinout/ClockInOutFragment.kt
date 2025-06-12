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
import za.co.skyner.estateguard.data.repository.FirebaseRepository
import za.co.skyner.estateguard.databinding.FragmentClockInOutBinding
import za.co.skyner.estateguard.ui.qr.QRScannerActivity
import za.co.skyner.estateguard.utils.LocationManager

class ClockInOutFragment : Fragment() {

    private var _binding: FragmentClockInOutBinding? = null
    private val binding get() = _binding!!

    private lateinit var clockInOutViewModel: ClockInOutViewModel
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var repository: FirebaseRepository
    private lateinit var locationManager: LocationManager

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
        repository = FirebaseRepository()
        locationManager = LocationManager(requireContext())

        // Create ViewModel with dependencies
        clockInOutViewModel = ClockInOutViewModelFactory(
            authManager, repository, locationManager
        ).create(ClockInOutViewModel::class.java)

        setupObservers()
        setupClickListeners()

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
            val intent = Intent(requireContext(), QRScannerActivity::class.java)
            qrScannerLauncher.launch(intent)
        }

        // Manual clock in/out
        binding.buttonManualEntry.setOnClickListener {
            clockInOutViewModel.manualClockInOut()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ViewModel Factory
class ClockInOutViewModelFactory(
    private val authManager: FirebaseAuthManager,
    private val repository: FirebaseRepository,
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
