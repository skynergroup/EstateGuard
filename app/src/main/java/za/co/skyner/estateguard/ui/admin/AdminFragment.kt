package za.co.skyner.estateguard.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import za.co.skyner.estateguard.auth.AuthResult
import za.co.skyner.estateguard.auth.FirebaseAuthManager
import za.co.skyner.estateguard.data.model.User
import za.co.skyner.estateguard.data.model.Incident
import za.co.skyner.estateguard.data.model.UserRole
import za.co.skyner.estateguard.databinding.FragmentAdminBinding

class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var adminViewModel: AdminViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        adminViewModel = ViewModelProvider(this)[AdminViewModel::class.java]
        _binding = FragmentAdminBinding.inflate(inflater, container, false)

        setupObservers()
        setupClickListeners()

        return binding.root
    }

    private fun setupObservers() {
        adminViewModel.totalUsers.observe(viewLifecycleOwner) { count ->
            binding.textTotalUsers.text = count.toString()
        }

        adminViewModel.totalIncidents.observe(viewLifecycleOwner) { count ->
            binding.textTotalIncidents.text = count.toString()
        }

        adminViewModel.activeGuards.observe(viewLifecycleOwner) { count ->
            binding.textActiveGuards.text = count.toString()
        }

        adminViewModel.todayIncidents.observe(viewLifecycleOwner) { count ->
            binding.textTodayIncidents.text = count.toString()
        }

        adminViewModel.guardsOnDuty.observe(viewLifecycleOwner) { guards ->
            updateGuardsOnDuty(guards)
        }

        adminViewModel.recentIncidents.observe(viewLifecycleOwner) { incidents ->
            updateRecentIncidents(incidents)
        }

        adminViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.buttonRefresh.isEnabled = !isLoading
        }
    }

    private fun updateGuardsOnDuty(guards: List<User>) {
        if (guards.isEmpty()) {
            binding.recyclerGuardsOnDuty.visibility = android.view.View.GONE
            binding.textNoGuardsOnDuty.visibility = android.view.View.VISIBLE
        } else {
            binding.recyclerGuardsOnDuty.visibility = android.view.View.VISIBLE
            binding.textNoGuardsOnDuty.visibility = android.view.View.GONE
            // TODO: Set up RecyclerView adapter for guards
        }
    }

    private fun updateRecentIncidents(incidents: List<Incident>) {
        if (incidents.isEmpty()) {
            binding.recyclerRecentIncidents.visibility = android.view.View.GONE
            binding.textNoIncidents.visibility = android.view.View.VISIBLE
        } else {
            binding.recyclerRecentIncidents.visibility = android.view.View.VISIBLE
            binding.textNoIncidents.visibility = android.view.View.GONE
            // TODO: Set up RecyclerView adapter for incidents
        }
    }

    private fun setupClickListeners() {
        // Handle card clicks for adding users
        binding.cardAddGuard.setOnClickListener {
            showUserRegistrationDialog(UserRole.SECURITY_GUARD)
        }

        binding.cardAddAdmin.setOnClickListener {
            showUserRegistrationDialog(UserRole.ADMIN)
        }

        binding.buttonReports.setOnClickListener {
            showFeatureNotImplemented("Reports")
        }

        binding.buttonRefresh.setOnClickListener {
            adminViewModel.refreshData()
            Toast.makeText(requireContext(), "Data refreshed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUserRegistrationDialog(role: UserRole) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(
            android.R.layout.simple_list_item_2, null
        )

        // Create custom dialog layout
        val builder = AlertDialog.Builder(requireContext())
        val roleTitle = if (role == UserRole.ADMIN) "Register New Administrator" else "Register New Security Guard"
        builder.setTitle(roleTitle)

        // Create input fields
        val container = android.widget.LinearLayout(requireContext())
        container.orientation = android.widget.LinearLayout.VERTICAL
        container.setPadding(50, 40, 50, 10)

        val nameInput = EditText(requireContext())
        nameInput.hint = "Full Name"
        container.addView(nameInput)

        val emailInput = EditText(requireContext())
        emailInput.hint = "Email Address"
        emailInput.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        container.addView(emailInput)

        val passwordInput = EditText(requireContext())
        passwordInput.hint = "Password"
        passwordInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        container.addView(passwordInput)

        builder.setView(container)

        builder.setPositiveButton("Register") { _, _ ->
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                registerNewUser(name, email, password, role)
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun registerNewUser(name: String, email: String, password: String, role: UserRole) {
        val firebaseAuthManager = FirebaseAuthManager(requireContext())

        lifecycleScope.launch {
            when (val result = firebaseAuthManager.signUp(email, password, name, role)) {
                is AuthResult.Success -> {
                    Toast.makeText(requireContext(), "User registered successfully!", Toast.LENGTH_LONG).show()
                    adminViewModel.refreshData() // Refresh the admin dashboard
                }
                is AuthResult.Error -> {
                    Toast.makeText(requireContext(), "Registration failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
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
