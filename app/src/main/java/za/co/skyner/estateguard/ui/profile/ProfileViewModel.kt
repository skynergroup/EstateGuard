package za.co.skyner.estateguard.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    private val _profileText = MutableLiveData<String>().apply {
        value = "Profile Settings - Coming Soon\n\nFeatures:\n• Personal Information\n• Change Password\n• Notification Settings\n• App Preferences"
    }
    val profileText: LiveData<String> = _profileText
}
