package za.co.skyner.estateguard.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> = _userEmail

    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> = _userRole

    private val _employeeId = MutableLiveData<String>()
    val employeeId: LiveData<String> = _employeeId

    private val _lastLogin = MutableLiveData<String>()
    val lastLogin: LiveData<String> = _lastLogin

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true

            // Simulate loading user data
            // In real implementation, this would fetch from repository
            _userName.value = "John Security"
            _userEmail.value = "john.security@estateguard.com"
            _userRole.value = "Security Guard"
            _employeeId.value = "EG001"
            _lastLogin.value = "Today at 08:30"

            _isLoading.value = false
        }
    }

    fun refreshProfile() {
        loadUserProfile()
    }
}
