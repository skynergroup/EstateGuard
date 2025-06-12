package za.co.skyner.estateguard.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import za.co.skyner.estateguard.R
import za.co.skyner.estateguard.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Observe ViewModel data
        homeViewModel.welcomeText.observe(viewLifecycleOwner) {
            binding.textWelcome.text = it
        }

        homeViewModel.currentStatus.observe(viewLifecycleOwner) {
            binding.textCurrentStatus.text = it
        }

        homeViewModel.lastActivity.observe(viewLifecycleOwner) {
            binding.textLastActivity.text = it
        }

        homeViewModel.incidentCount.observe(viewLifecycleOwner) {
            binding.textIncidentCount.text = it
        }

        // Set up click listeners for quick actions
        binding.buttonQuickClockIn.setOnClickListener {
            findNavController().navigate(R.id.nav_clock_in_out)
        }

        binding.buttonQuickIncident.setOnClickListener {
            findNavController().navigate(R.id.nav_incident_log)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}