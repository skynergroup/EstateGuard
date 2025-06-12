package za.co.skyner.estateguard.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import za.co.skyner.estateguard.data.model.TimeEntry
import za.co.skyner.estateguard.data.model.TimeEntryType

@Dao
interface TimeEntryDao {
    
    @Query("SELECT * FROM time_entries WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTimeEntriesForUser(userId: String): LiveData<List<TimeEntry>>
    
    @Query("SELECT * FROM time_entries WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastTimeEntryForUser(userId: String): TimeEntry?
    
    @Query("SELECT * FROM time_entries WHERE userId = :userId AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getTimeEntriesForUserInRange(userId: String, startTime: Long, endTime: Long): List<TimeEntry>
    
    @Query("SELECT * FROM time_entries ORDER BY timestamp DESC")
    fun getAllTimeEntries(): LiveData<List<TimeEntry>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeEntry(timeEntry: TimeEntry)
    
    @Update
    suspend fun updateTimeEntry(timeEntry: TimeEntry)
    
    @Delete
    suspend fun deleteTimeEntry(timeEntry: TimeEntry)
    
    @Query("DELETE FROM time_entries WHERE userId = :userId")
    suspend fun deleteAllTimeEntriesForUser(userId: String)
}
