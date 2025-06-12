package za.co.skyner.estateguard.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import za.co.skyner.estateguard.data.dao.IncidentDao
import za.co.skyner.estateguard.data.dao.TimeEntryDao
import za.co.skyner.estateguard.data.dao.UserDao
import za.co.skyner.estateguard.data.model.Incident
import za.co.skyner.estateguard.data.model.TimeEntry
import za.co.skyner.estateguard.data.model.User

@Database(
    entities = [User::class, TimeEntry::class, Incident::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EstateGuardDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun timeEntryDao(): TimeEntryDao
    abstract fun incidentDao(): IncidentDao
    
    companion object {
        @Volatile
        private var INSTANCE: EstateGuardDatabase? = null
        
        fun getDatabase(context: Context): EstateGuardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EstateGuardDatabase::class.java,
                    "estate_guard_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
