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
import za.co.skyner.estateguard.data.repository.FirebaseRepository
import za.co.skyner.estateguard.databinding.FragmentIncidentLogBinding
import za.co.skyner.estateguard.ui.camera.CameraCaptureActivity
import za.co.skyner.estateguard.utils.LocationManager

class IncidentLogFragment : Fragment() {

    private var _binding: FragmentIncidentLogBinding? = null
    private val binding get() = _binding!!

    private lateinit var incidentLogViewModel: IncidentLogViewModel
    private lateinit var authManager: FirebaseAuthManager
    private lateinit var repository: FirebaseRepository
    private lateinit var locationManager: LocationManager
    private lateinit var incidentAdapter: IncidentAdapter

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
        repository = FirebaseRepository()
        locationManager = LocationManager(requireContext())

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
            val intent = Intent(requireContext(), CameraCaptureActivity::class.java)
            cameraCaptureLauncher.launch(intent)
        }

        // Submit incident
        binding.buttonSubmitIncident.setOnClickListener {
            val description = binding.editTextDescription.text.toString().trim()
            if (description.isNotEmpty()) {
                val photoUri = incidentLogViewModel.capturedPhotoUri.value
                incidentLogViewModel.submitIncident(
                    description = description,
                    severity = IncidentSeverity.LOW, // Default severity
                    photoUri = photoUri
                )
            } else {
                Toast.makeText(requireContext(), "Please provide an incident description", Toast.LENGTH_SHORT).show()
            }
        }

        // Remove photo
        binding.buttonRemovePhoto.setOnClickListener {
            incidentLogViewModel.clearPhoto()
        }
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
    private val repository: FirebaseRepository,
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
