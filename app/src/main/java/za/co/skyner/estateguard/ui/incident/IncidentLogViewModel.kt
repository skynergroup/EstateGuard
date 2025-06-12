package za.co.skyner.estateguard.ui.incident

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

data class Incident(
    val id: String,
    val description: String,
    val timestamp: String,
    val location: String? = null,
    val photoPath: String? = null
)

class IncidentLogViewModel : ViewModel() {

    private val _incidents = MutableLiveData<List<Incident>>().apply {
        value = emptyList()
    }
    val incidents: LiveData<List<Incident>> = _incidents

    private val _submitStatus = MutableLiveData<String>()
    val submitStatus: LiveData<String> = _submitStatus

    fun submitIncident(description: String, photoPath: String? = null, location: String? = null) {
        val currentTime = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(Date())
        val newIncident = Incident(
            id = UUID.randomUUID().toString(),
            description = description,
            timestamp = currentTime,
            location = location ?: "Location not available",
            photoPath = photoPath
        )

        val currentList = _incidents.value?.toMutableList() ?: mutableListOf()
        currentList.add(0, newIncident) // Add to beginning of list
        _incidents.value = currentList

        _submitStatus.value = "Incident logged successfully"
    }

    fun addSampleIncident() {
        submitIncident(
            description = "Sample incident for testing",
            location = "Main Gate"
        )
    }

    fun getIncidentCount(): Int {
        return _incidents.value?.size ?: 0
    }
}
