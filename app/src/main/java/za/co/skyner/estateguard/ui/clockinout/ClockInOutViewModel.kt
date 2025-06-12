package za.co.skyner.estateguard.ui.clockinout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import za.co.skyner.estateguard.auth.FirebaseAuthManager
import za.co.skyner.estateguard.data.model.TimeEntry
import za.co.skyner.estateguard.data.model.TimeEntryType
import za.co.skyner.estateguard.data.repository.FirebaseRepository
import za.co.skyner.estateguard.utils.LocationManager
import za.co.skyner.estateguard.utils.LocationResult
import java.text.SimpleDateFormat
import java.util.*

class ClockInOutViewModel(
    private val authManager: FirebaseAuthManager,
    private val repository: FirebaseRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _clockStatus = MutableLiveData<String>()
    val clockStatus: LiveData<String> = _clockStatus

    private val _lastActivity = MutableLiveData<String>()
    val lastActivity: LiveData<String> = _lastActivity

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var currentUser = authManager.getCurrentFirebaseUser()

    init {
        loadCurrentStatus()
    }

    private fun loadCurrentStatus() {
        viewModelScope.launch {
            _isLoading.value = true

            currentUser?.let { user ->
                val result = repository.getLastTimeEntryForUser(user.uid)
                result.fold(
                    onSuccess = { lastEntry ->
                        if (lastEntry != null) {
                            val isClockedIn = lastEntry.type == TimeEntryType.CLOCK_IN
                            _clockStatus.value = if (isClockedIn) "Clocked In" else "Clocked Out"

                            val timeFormat = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
                            val timeString = timeFormat.format(Date(lastEntry.timestamp))
                            val action = if (isClockedIn) "Clocked in" else "Clocked out"
                            _lastActivity.value = "$action at $timeString"
                        } else {
                            _clockStatus.value = "Clocked Out"
                            _lastActivity.value = "No recent activity"
                        }
                    },
                    onFailure = { exception ->
                        _errorMessage.value = "Failed to load status: ${exception.message}"
                        _clockStatus.value = "Clocked Out"
                        _lastActivity.value = "No recent activity"
                    }
                )
            }

            _isLoading.value = false
        }
    }

    fun processQRScan(qrData: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // Get current location
            val locationResult = locationManager.getCurrentLocation()

            when (locationResult) {
                is LocationResult.Success -> {
                    createTimeEntry(
                        qrCodeData = qrData,
                        latitude = locationResult.latitude,
                        longitude = locationResult.longitude,
                        isManualEntry = false
                    )
                }
                is LocationResult.Error -> {
                    // Proceed without location
                    createTimeEntry(
                        qrCodeData = qrData,
                        latitude = null,
                        longitude = null,
                        isManualEntry = false
                    )
                }
            }
        }
    }

    fun manualClockInOut() {
        viewModelScope.launch {
            _isLoading.value = true

            // Get current location
            val locationResult = locationManager.getCurrentLocation()

            when (locationResult) {
                is LocationResult.Success -> {
                    createTimeEntry(
                        qrCodeData = null,
                        latitude = locationResult.latitude,
                        longitude = locationResult.longitude,
                        isManualEntry = true
                    )
                }
                is LocationResult.Error -> {
                    // Proceed without location
                    createTimeEntry(
                        qrCodeData = null,
                        latitude = null,
                        longitude = null,
                        isManualEntry = true
                    )
                }
            }
        }
    }

    private suspend fun createTimeEntry(
        qrCodeData: String?,
        latitude: Double?,
        longitude: Double?,
        isManualEntry: Boolean
    ) {
        currentUser?.let { user ->
            // Determine if this should be clock in or clock out
            val lastEntryResult = repository.getLastTimeEntryForUser(user.uid)
            val shouldClockIn = lastEntryResult.fold(
                onSuccess = { lastEntry ->
                    lastEntry?.type != TimeEntryType.CLOCK_IN
                },
                onFailure = { true } // Default to clock in if we can't determine
            )

            val timeEntry = TimeEntry(
                id = UUID.randomUUID().toString(),
                userId = user.uid,
                timestamp = System.currentTimeMillis(),
                type = if (shouldClockIn) TimeEntryType.CLOCK_IN else TimeEntryType.CLOCK_OUT,
                latitude = latitude,
                longitude = longitude,
                location = getLocationString(latitude, longitude),
                qrCodeData = qrCodeData,
                isManualEntry = isManualEntry
            )

            val result = repository.saveTimeEntry(timeEntry)
            result.fold(
                onSuccess = {
                    // Update UI
                    val action = if (shouldClockIn) "Clocked in" else "Clocked out"
                    val timeFormat = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
                    val timeString = timeFormat.format(Date(timeEntry.timestamp))

                    _clockStatus.value = if (shouldClockIn) "Clocked In" else "Clocked Out"
                    _lastActivity.value = "$action at $timeString"
                },
                onFailure = { exception ->
                    _errorMessage.value = "Failed to save time entry: ${exception.message}"
                }
            )
        }

        _isLoading.value = false
    }

    private fun getLocationString(latitude: Double?, longitude: Double?): String? {
        return if (latitude != null && longitude != null) {
            "Lat: ${"%.6f".format(latitude)}, Lng: ${"%.6f".format(longitude)}"
        } else {
            null
        }
    }

    fun getCurrentStatus(): Boolean {
        return _clockStatus.value == "Clocked In"
    }
}
