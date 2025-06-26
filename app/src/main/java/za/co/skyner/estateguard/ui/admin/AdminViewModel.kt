package za.co.skyner.estateguard.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _totalUsers = MutableLiveData<Int>()
    val totalUsers: LiveData<Int> = _totalUsers

    private val _totalIncidents = MutableLiveData<Int>()
    val totalIncidents: LiveData<Int> = _totalIncidents

    private val _activeGuards = MutableLiveData<Int>()
    val activeGuards: LiveData<Int> = _activeGuards

    private val _todayIncidents = MutableLiveData<Int>()
    val todayIncidents: LiveData<Int> = _todayIncidents

    init {
        loadAdminData()
    }

    private fun loadAdminData() {
        viewModelScope.launch {
            _isLoading.value = true

            // Simulate loading admin data
            // In real implementation, this would fetch from repository
            _totalUsers.value = 25
            _totalIncidents.value = 142
            _activeGuards.value = 8
            _todayIncidents.value = 3

            _isLoading.value = false
        }
    }

    fun refreshData() {
        loadAdminData()
    }
}
