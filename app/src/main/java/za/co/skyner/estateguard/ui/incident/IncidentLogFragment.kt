package za.co.skyner.estateguard.ui.incident

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import za.co.skyner.estateguard.auth.FirebaseAuthManager
import za.co.skyner.estateguard.data.model.IncidentSeverity
import za.co.skyner.estateguard.data.repository.EstateGuardRepository
import za.co.skyner.estateguard.data.repository.RepositoryProvider
import za.co.skyner.estateguard.databinding.FragmentIncidentLogBinding
import za.co.skyner.estateguard.ui.camera.CameraCaptureActivity
import za.co.skyner.estateguard.ui.permissions.PermissionDialogFragment
import za.co.skyner.estateguard.utils.FragmentPermissionHelper
import za.co.skyner.estateguard.utils.LocationManager
import za.co.skyner.estateguard.utils.PermissionManager

class IncidentLogFragment : Fragment() {

    private var _binding: FragmentIncidentLogBinding? = null
    private val binding get() = _binding!!

    private lateinit var incidentLogViewModel: IncidentLogViewModel
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var repository: EstateGuardRepository
    private lateinit var locationManager: LocationManager
    private lateinit var incidentAdapter: IncidentAdapter
    private lateinit var permissionHelper: FragmentPermissionHelper
    private lateinit var permissionManager: PermissionManager

    // Camera capture result launcher
    private val cameraCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photoUriString = result.data?.getStringExtra(CameraCaptureActivity.EXTRA_PHOTO_URI)
            photoUriString?.let { uriString ->
                val photoUri = Uri.parse(uriString)
                incidentLogViewModel.setPhotoUri(photoUri)
                displayCapturedPhoto(photoUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncidentLogBinding.inflate(inflater, container, false)

        // Initialize dependencies
        authManager = FirebaseAuthManager(requireContext())
        repository = RepositoryProvider.getRepository(requireContext())
        locationManager = LocationManager(requireContext())
        permissionManager = PermissionManager(requireContext())
        permissionHelper = FragmentPermissionHelper(this)

        // Register permission launcher
        permissionHelper.registerPermissionLauncher()

        // Create ViewModel with dependencies
        incidentLogViewModel = IncidentLogViewModelFactory(
            authManager, repository, locationManager
        ).create(IncidentLogViewModel::class.java)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        return binding.root
    }

    private fun setupRecyclerView() {
        incidentAdapter = IncidentAdapter()
        binding.recyclerViewIncidents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = incidentAdapter
        }
    }

    private fun setupObservers() {
        // Observe incidents list
        incidentLogViewModel.incidents.observe(viewLifecycleOwner) { incidents ->
            incidentAdapter.submitList(incidents)
            binding.textNoIncidents.visibility = if (incidents.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe loading state
        incidentLogViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.buttonSubmitIncident.isEnabled = !isLoading
            binding.buttonTakePhoto.isEnabled = !isLoading
        }

        // Observe submit status
        incidentLogViewModel.submitStatus.observe(viewLifecycleOwner) { status ->
            if (status.isNotEmpty()) {
                Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show()
                // Clear form after successful submission
                binding.editTextDescription.text?.clear()
                binding.imageViewPhoto.visibility = View.GONE
                binding.buttonRemovePhoto.visibility = View.GONE
                incidentLogViewModel.clearMessages()
            }
        }

        // Observe error messages
        incidentLogViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                incidentLogViewModel.clearMessages()
            }
        }

        // Observe captured photo
        incidentLogViewModel.capturedPhotoUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                displayCapturedPhoto(uri)
            } else {
                binding.imageViewPhoto.visibility = View.GONE
                binding.buttonRemovePhoto.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        // Take photo
        binding.buttonTakePhoto.setOnClickListener {
            checkPermissionsAndTakePhoto()
        }

        // Submit incident
        binding.buttonSubmitIncident.setOnClickListener {
            checkPermissionsAndSubmitIncident()
        }

        // Remove photo
        binding.buttonRemovePhoto.setOnClickListener {
            incidentLogViewModel.clearPhoto()
        }
    }

    private fun checkPermissionsAndTakePhoto() {
        if (permissionManager.hasCameraPermission()) {
            launchCamera()
        } else {
            requestCameraPermission { granted ->
                if (granted) {
                    launchCamera()
                } else {
                    showPermissionDeniedMessage("Camera permission is required to take photos")
                }
            }
        }
    }

    private fun checkPermissionsAndSubmitIncident() {
        val description = binding.editTextDescription.text.toString().trim()
        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Please provide an incident description", Toast.LENGTH_SHORT).show()
            return
        }

        if (permissionManager.hasLocationPermission()) {
            submitIncident(description)
        } else {
            requestLocationPermission { granted ->
                if (granted) {
                    submitIncident(description)
                } else {
                    // Submit without location
                    Toast.makeText(requireContext(), "Submitting incident without location data", Toast.LENGTH_SHORT).show()
                    submitIncident(description)
                }
            }
        }
    }

    private fun launchCamera() {
        val intent = Intent(requireContext(), CameraCaptureActivity::class.java)
        cameraCaptureLauncher.launch(intent)
    }

    private fun submitIncident(description: String) {
        val photoUri = incidentLogViewModel.capturedPhotoUri.value
        incidentLogViewModel.submitIncident(
            description = description,
            severity = IncidentSeverity.LOW, // Default severity
            photoUri = photoUri
        )
    }

    private fun requestCameraPermission(onResult: (Boolean) -> Unit) {
        permissionHelper.requestCameraPermission(onResult)
    }

    private fun requestLocationPermission(onResult: (Boolean) -> Unit) {
        permissionHelper.requestLocationPermission(onResult)
    }

    private fun showPermissionDeniedMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun displayCapturedPhoto(uri: Uri) {
        binding.imageViewPhoto.visibility = View.VISIBLE
        binding.buttonRemovePhoto.visibility = View.VISIBLE

        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.imageViewPhoto)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ViewModel Factory
class IncidentLogViewModelFactory(
    private val authManager: FirebaseAuthManager,
    private val repository: EstateGuardRepository,
    private val locationManager: LocationManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncidentLogViewModel::class.java)) {
            return IncidentLogViewModel(authManager, repository, locationManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
