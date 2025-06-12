package za.co.skyner.estateguard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incidents")
data class Incident(
    @PrimaryKey
    val id: String,
    val userId: String,
    val description: String,
    val timestamp: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val location: String? = null,
    val photoPath: String? = null,
    val severity: IncidentSeverity = IncidentSeverity.LOW,
    val status: IncidentStatus = IncidentStatus.REPORTED
)

enum class IncidentSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class IncidentStatus {
    REPORTED,
    INVESTIGATING,
    RESOLVED,
    CLOSED
}
