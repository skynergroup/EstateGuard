package za.co.skyner.estateguard.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import za.co.skyner.estateguard.data.model.Incident
import za.co.skyner.estateguard.data.model.IncidentStatus

@Dao
interface IncidentDao {
    
    @Query("SELECT * FROM incidents ORDER BY timestamp DESC")
    fun getAllIncidents(): LiveData<List<Incident>>
    
    @Query("SELECT * FROM incidents WHERE userId = :userId ORDER BY timestamp DESC")
    fun getIncidentsForUser(userId: String): LiveData<List<Incident>>
    
    @Query("SELECT * FROM incidents WHERE id = :incidentId")
    suspend fun getIncidentById(incidentId: String): Incident?
    
    @Query("SELECT * FROM incidents WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getIncidentsInRange(startTime: Long, endTime: Long): List<Incident>
    
    @Query("SELECT * FROM incidents WHERE status = :status ORDER BY timestamp DESC")
    fun getIncidentsByStatus(status: IncidentStatus): LiveData<List<Incident>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: Incident)
    
    @Update
    suspend fun updateIncident(incident: Incident)
    
    @Delete
    suspend fun deleteIncident(incident: Incident)
    
    @Query("DELETE FROM incidents WHERE userId = :userId")
    suspend fun deleteAllIncidentsForUser(userId: String)
    
    @Query("SELECT COUNT(*) FROM incidents WHERE userId = :userId AND timestamp >= :startTime")
    suspend fun getIncidentCountForUserSince(userId: String, startTime: Long): Int
}
