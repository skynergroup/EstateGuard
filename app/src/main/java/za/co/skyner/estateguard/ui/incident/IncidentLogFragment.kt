package za.co.skyner.estateguard.ui.incident

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import za.co.skyner.estateguard.databinding.FragmentIncidentLogBinding

class IncidentLogFragment : Fragment() {

    private var _binding: FragmentIncidentLogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val incidentLogViewModel =
            ViewModelProvider(this).get(IncidentLogViewModel::class.java)

        _binding = FragmentIncidentLogBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Observe ViewModel data
        incidentLogViewModel.incidents.observe(viewLifecycleOwner) { incidents ->
            // TODO: Update RecyclerView with incidents
        }

        // Set up click listeners
        binding.buttonTakePhoto.setOnClickListener {
            // TODO: Implement camera functionality
            incidentLogViewModel.addSampleIncident()
        }

        binding.buttonSubmitIncident.setOnClickListener {
            val description = binding.editTextDescription.text.toString()
            if (description.isNotEmpty()) {
                incidentLogViewModel.submitIncident(description)
                binding.editTextDescription.text?.clear()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
