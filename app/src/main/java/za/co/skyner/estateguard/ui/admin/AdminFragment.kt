package za.co.skyner.estateguard.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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

        adminViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You can add a progress bar here if needed
            binding.buttonRefresh.isEnabled = !isLoading
        }
    }

    private fun setupClickListeners() {
        binding.buttonUserManagement.setOnClickListener {
            showFeatureNotImplemented("User Management")
        }

        binding.buttonReports.setOnClickListener {
            showFeatureNotImplemented("Reports")
        }

        binding.buttonExportData.setOnClickListener {
            showFeatureNotImplemented("Data Export")
        }

        binding.buttonSettings.setOnClickListener {
            showFeatureNotImplemented("Settings")
        }

        binding.buttonRefresh.setOnClickListener {
            adminViewModel.refreshData()
            Toast.makeText(requireContext(), "Data refreshed", Toast.LENGTH_SHORT).show()
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
