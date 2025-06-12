package za.co.skyner.estateguard.ui.clockinout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import za.co.skyner.estateguard.databinding.FragmentClockInOutBinding

class ClockInOutFragment : Fragment() {

    private var _binding: FragmentClockInOutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val clockInOutViewModel =
            ViewModelProvider(this).get(ClockInOutViewModel::class.java)

        _binding = FragmentClockInOutBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Observe ViewModel data
        clockInOutViewModel.clockStatus.observe(viewLifecycleOwner) { status ->
            binding.textClockStatus.text = status
        }

        clockInOutViewModel.lastActivity.observe(viewLifecycleOwner) { activity ->
            binding.textLastActivity.text = activity
        }

        // Set up click listeners
        binding.buttonScanQr.setOnClickListener {
            // TODO: Implement QR code scanning
            clockInOutViewModel.toggleClockStatus()
        }

        binding.buttonManualEntry.setOnClickListener {
            // TODO: Implement manual clock in/out
            clockInOutViewModel.toggleClockStatus()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
