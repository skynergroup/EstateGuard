package za.co.skyner.estateguard.ui.incident

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import za.co.skyner.estateguard.auth.FirebaseAuthManager
import za.co.skyner.estateguard.data.model.Incident
import za.co.skyner.estateguard.data.model.IncidentSeverity
import za.co.skyner.estateguard.data.model.IncidentStatus
import za.co.skyner.estateguard.data.repository.EstateGuardRepository
import za.co.skyner.estateguard.utils.LocationManager
import za.co.skyner.estateguard.utils.LocationResult
import java.util.*

class IncidentLogViewModel(
    private val authManager: FirebaseAuthManager,
    private val repository: EstateGuardRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _submitStatus = MutableLiveData<String>()
    val submitStatus: LiveData<String> = _submitStatus

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _capturedPhotoUri = MutableLiveData<Uri?>()
    val capturedPhotoUri: LiveData<Uri?> = _capturedPhotoUri

    private val currentUser = authManager.getCurrentFirebaseUser()

    // Get incidents for current user
    val incidents: LiveData<List<Incident>> = currentUser?.let { user ->
        repository.getIncidentsForUser(user.uid)
    } ?: MutableLiveData(emptyList())

    fun submitIncident(
        description: String,
        severity: IncidentSeverity = IncidentSeverity.LOW,
        photoUri: Uri? = null
    ) {
        if (description.isBlank()) {
            _errorMessage.value = "Please provide an incident description"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            currentUser?.let { user ->
                try {
                    // Get current location
                    val locationResult = locationManager.getCurrentLocation()
                    val (latitude, longitude) = when (locationResult) {
                        is LocationResult.Success -> Pair(locationResult.latitude, locationResult.longitude)
                        is LocationResult.Error -> Pair(null, null)
                    }

                    val incidentId = UUID.randomUUID().toString()

                    // Upload photo if provided
                    var photoUrl: String? = null
                    if (photoUri != null) {
                        val uploadResult = repository.uploadIncidentPhoto(photoUri, incidentId)
                        uploadResult.fold(
                            onSuccess = { url -> photoUrl = url },
                            onFailure = {
                                _errorMessage.value = "Failed to upload photo, but incident will be saved without it"
                            }
                        )
                    }

                    // Create incident
                    val incident = Incident(
                        id = incidentId,
                        userId = user.uid,
                        description = description,
                        timestamp = System.currentTimeMillis(),
                        latitude = latitude,
                        longitude = longitude,
                        location = getLocationString(latitude, longitude),
                        photoPath = photoUrl,
                        severity = severity,
                        status = IncidentStatus.REPORTED
                    )

                    // Save incident
                    val saveResult = repository.saveIncident(incident)
                    saveResult.fold(
                        onSuccess = {
                            _submitStatus.value = "Incident logged successfully"
                            _capturedPhotoUri.value = null // Clear photo after successful submission
                        },
                        onFailure = { exception ->
                            _errorMessage.value = "Failed to save incident: ${exception.message}"
                        }
                    )

                } catch (e: Exception) {
                    _errorMessage.value = "An error occurred: ${e.message}"
                }
            } ?: run {
                _errorMessage.value = "User not authenticated"
            }

            _isLoading.value = false
        }
    }

    fun setPhotoUri(uri: Uri?) {
        _capturedPhotoUri.value = uri
    }

    fun clearPhoto() {
        _capturedPhotoUri.value = null
    }

    private fun getLocationString(latitude: Double?, longitude: Double?): String? {
        return if (latitude != null && longitude != null) {
            "Lat: ${"%.6f".format(latitude)}, Lng: ${"%.6f".format(longitude)}"
        } else {
            "Location not available"
        }
    }

    fun getIncidentCount(): Int {
        return incidents.value?.size ?: 0
    }

    fun clearMessages() {
        _submitStatus.value = ""
        _errorMessage.value = ""
    }
}
