package za.co.skyner.estateguard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_entries")
data class TimeEntry(
    @PrimaryKey
    val id: String,
    val userId: String,
    val timestamp: Long,
    val type: TimeEntryType,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val location: String? = null,
    val qrCodeData: String? = null,
    val isManualEntry: Boolean = false
)

enum class TimeEntryType {
    CLOCK_IN,
    CLOCK_OUT
}
