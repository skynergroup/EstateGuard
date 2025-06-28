package za.co.skyner.estateguard.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import za.co.skyner.estateguard.LoginActivity
import za.co.skyner.estateguard.auth.AuthManager
import za.co.skyner.estateguard.auth.FirebaseAuthManager
import za.co.skyner.estateguard.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        setupObservers()
        setupClickListeners()

        return binding.root
    }

    private fun setupObservers() {
        profileViewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.textUserName.text = name
        }

        profileViewModel.userEmail.observe(viewLifecycleOwner) { email ->
            binding.textUserEmail.text = email
        }

        profileViewModel.userRole.observe(viewLifecycleOwner) { role ->
            binding.textUserRole.text = role
        }

        profileViewModel.employeeId.observe(viewLifecycleOwner) { id ->
            binding.textEmployeeId.text = id
        }

        profileViewModel.lastLogin.observe(viewLifecycleOwner) { lastLogin ->
            binding.textLastLogin.text = lastLogin
        }

        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You can add a progress bar here if needed
        }
    }

    private fun setupClickListeners() {
        binding.buttonEditProfile.setOnClickListener {
            showFeatureNotImplemented("Edit Profile")
        }

        binding.buttonChangePassword.setOnClickListener {
            showFeatureNotImplemented("Change Password")
        }

        binding.buttonNotifications.setOnClickListener {
            showFeatureNotImplemented("Notification Settings")
        }

        binding.buttonLogout.setOnClickListener {
            // Handle logout
            performLogout()
        }
    }

    private fun performLogout() {
        try {
            // Sign out from Firebase (this also clears local auth state)
            val firebaseAuthManager = FirebaseAuthManager(requireContext())
            firebaseAuthManager.signOut()

            // Show logout confirmation
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Navigate to login activity
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // Finish the current activity
            requireActivity().finish()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error during logout: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showFeatureNotImplemented(featureName: String) {
        Toast.makeText(
            requireContext(),
            "$featureName feature will be implemented in future updates",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
