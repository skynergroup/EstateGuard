package za.co.skyner.estateguard.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AdminViewModel : ViewModel() {

    private val _adminText = MutableLiveData<String>().apply {
        value = "Admin Panel - Coming Soon\n\nFeatures:\n• User Management\n• Reports & Analytics\n• Data Export\n• System Settings"
    }
    val adminText: LiveData<String> = _adminText
}
