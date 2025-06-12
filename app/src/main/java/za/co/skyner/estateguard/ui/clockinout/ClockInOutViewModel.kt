package za.co.skyner.estateguard.ui.clockinout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class ClockInOutViewModel : ViewModel() {

    private val _clockStatus = MutableLiveData<String>().apply {
        value = "Clocked Out"
    }
    val clockStatus: LiveData<String> = _clockStatus

    private val _lastActivity = MutableLiveData<String>().apply {
        value = "No recent activity"
    }
    val lastActivity: LiveData<String> = _lastActivity

    private var isClockedIn = false

    fun toggleClockStatus() {
        val currentTime = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(Date())
        
        if (isClockedIn) {
            // Clock out
            _clockStatus.value = "Clocked Out"
            _lastActivity.value = "Clocked out at $currentTime"
            isClockedIn = false
        } else {
            // Clock in
            _clockStatus.value = "Clocked In"
            _lastActivity.value = "Clocked in at $currentTime"
            isClockedIn = true
        }
    }

    fun getCurrentStatus(): Boolean {
        return isClockedIn
    }
}
