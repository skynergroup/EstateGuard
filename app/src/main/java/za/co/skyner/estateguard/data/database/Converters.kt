package za.co.skyner.estateguard.data.database

import androidx.room.TypeConverter
import za.co.skyner.estateguard.data.model.IncidentSeverity
import za.co.skyner.estateguard.data.model.IncidentStatus
import za.co.skyner.estateguard.data.model.TimeEntryType
import za.co.skyner.estateguard.data.model.UserRole

class Converters {
    
    @TypeConverter
    fun fromUserRole(role: UserRole): String {
        return role.name
    }
    
    @TypeConverter
    fun toUserRole(role: String): UserRole {
        return UserRole.valueOf(role)
    }
    
    @TypeConverter
    fun fromTimeEntryType(type: TimeEntryType): String {
        return type.name
    }
    
    @TypeConverter
    fun toTimeEntryType(type: String): TimeEntryType {
        return TimeEntryType.valueOf(type)
    }
    
    @TypeConverter
    fun fromIncidentSeverity(severity: IncidentSeverity): String {
        return severity.name
    }
    
    @TypeConverter
    fun toIncidentSeverity(severity: String): IncidentSeverity {
        return IncidentSeverity.valueOf(severity)
    }
    
    @TypeConverter
    fun fromIncidentStatus(status: IncidentStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toIncidentStatus(status: String): IncidentStatus {
        return IncidentStatus.valueOf(status)
    }
}
