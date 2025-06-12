package za.co.skyner.estateguard.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel : ViewModel() {

    private val _welcomeText = MutableLiveData<String>().apply {
        value = "Welcome to EstateGuard"
    }
    val welcomeText: LiveData<String> = _welcomeText

    private val _currentStatus = MutableLiveData<String>().apply {
        value = "Clocked Out"
    }
    val currentStatus: LiveData<String> = _currentStatus

    private val _lastActivity = MutableLiveData<String>().apply {
        val currentTime = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(Date())
        value = "Last activity: $currentTime"
    }
    val lastActivity: LiveData<String> = _lastActivity

    private val _incidentCount = MutableLiveData<String>().apply {
        value = "0 incidents logged today"
    }
    val incidentCount: LiveData<String> = _incidentCount

    fun updateStatus(isClocked: Boolean) {
        _currentStatus.value = if (isClocked) "Clocked In" else "Clocked Out"
    }

    fun updateIncidentCount(count: Int) {
        _incidentCount.value = "$count incidents logged today"
    }
}