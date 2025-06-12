package za.co.skyner.estateguard.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import za.co.skyner.estateguard.databinding.FragmentAdminBinding

class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val adminViewModel =
            ViewModelProvider(this).get(AdminViewModel::class.java)

        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        val root: View = binding.root

        adminViewModel.adminText.observe(viewLifecycleOwner) {
            binding.textAdmin.text = it
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
