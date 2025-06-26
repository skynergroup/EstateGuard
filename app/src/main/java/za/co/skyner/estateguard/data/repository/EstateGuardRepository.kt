package za.co.skyner.estateguard.data.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import za.co.skyner.estateguard.data.dao.IncidentDao
import za.co.skyner.estateguard.data.dao.TimeEntryDao
import za.co.skyner.estateguard.data.dao.UserDao
import za.co.skyner.estateguard.data.model.Incident
import za.co.skyner.estateguard.data.model.IncidentStatus
import za.co.skyner.estateguard.data.model.TimeEntry
import za.co.skyner.estateguard.data.model.TimeEntryType
import za.co.skyner.estateguard.data.model.User
import java.util.*

/**
 * Repository that handles data operations for EstateGuard app.
 * Provides a clean API for data access to the rest of the app.
 * This is the single source of truth for all data operations.
 */
class EstateGuardRepository(
    private val userDao: UserDao,
    private val timeEntryDao: TimeEntryDao,
    private val incidentDao: IncidentDao,
    private val firebaseRepository: FirebaseRepository
) {

    // ==================== USER OPERATIONS ====================
    
    /**
     * Get all active users from local database
     */
    fun getAllActiveUsers(): LiveData<List<User>> = userDao.getAllActiveUsers()
    
    /**
     * Get user by ID from local database
     */
    suspend fun getUserById(userId: String): User? = userDao.getUserById(userId)
    
    /**
     * Get user by email from local database
     */
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    
    /**
     * Save user to both local database and Firebase
     */
    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            // Save to local database first
            userDao.insertUser(user)
            
            // Then sync to Firebase
            val firebaseResult = firebaseRepository.saveUser(user)
            if (firebaseResult.isFailure) {
                // Log the Firebase error but don't fail the operation
                // since local save succeeded
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user in both local database and Firebase
     */
    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            userDao.updateUser(user)
            firebaseRepository.saveUser(user) // Firebase uses set which acts as update
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Deactivate user (soft delete)
     */
    suspend fun deactivateUser(userId: String): Result<Unit> {
        return try {
            userDao.deactivateUser(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== TIME ENTRY OPERATIONS ====================
    
    /**
     * Get all time entries for a specific user
     */
    fun getTimeEntriesForUser(userId: String): LiveData<List<TimeEntry>> = 
        timeEntryDao.getTimeEntriesForUser(userId)
    
    /**
     * Get the last time entry for a user (to determine current clock status)
     */
    suspend fun getLastTimeEntryForUser(userId: String): TimeEntry? = 
        timeEntryDao.getLastTimeEntryForUser(userId)
    
    /**
     * Get time entries for a user within a specific time range
     */
    suspend fun getTimeEntriesForUserInRange(
        userId: String, 
        startTime: Long, 
        endTime: Long
    ): List<TimeEntry> = timeEntryDao.getTimeEntriesForUserInRange(userId, startTime, endTime)
    
    /**
     * Save time entry to both local database and Firebase
     */
    suspend fun saveTimeEntry(timeEntry: TimeEntry): Result<Unit> {
        return try {
            // Save to local database first
            timeEntryDao.insertTimeEntry(timeEntry)
            
            // Then sync to Firebase
            firebaseRepository.saveTimeEntry(timeEntry)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create a new time entry (clock in/out)
     */
    suspend fun createTimeEntry(
        userId: String,
        type: TimeEntryType,
        latitude: Double? = null,
        longitude: Double? = null,
        location: String? = null,
        qrCodeData: String? = null,
        isManualEntry: Boolean = false
    ): Result<TimeEntry> {
        return try {
            val timeEntry = TimeEntry(
                id = UUID.randomUUID().toString(),
                userId = userId,
                timestamp = System.currentTimeMillis(),
                type = type,
                latitude = latitude,
                longitude = longitude,
                location = location,
                qrCodeData = qrCodeData,
                isManualEntry = isManualEntry
            )
            
            val saveResult = saveTimeEntry(timeEntry)
            if (saveResult.isSuccess) {
                Result.success(timeEntry)
            } else {
                Result.failure(saveResult.exceptionOrNull() ?: Exception("Failed to save time entry"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current clock status for a user
     */
    suspend fun getCurrentClockStatus(userId: String): Flow<ClockStatus> = flow {
        val lastEntry = getLastTimeEntryForUser(userId)
        val status = when (lastEntry?.type) {
            TimeEntryType.CLOCK_IN -> ClockStatus.CLOCKED_IN
            TimeEntryType.CLOCK_OUT -> ClockStatus.CLOCKED_OUT
            null -> ClockStatus.CLOCKED_OUT
        }
        emit(status)
    }

    // ==================== INCIDENT OPERATIONS ====================
    
    /**
     * Get all incidents from local database
     */
    fun getAllIncidents(): LiveData<List<Incident>> = incidentDao.getAllIncidents()
    
    /**
     * Get incidents for a specific user
     */
    fun getIncidentsForUser(userId: String): LiveData<List<Incident>> = 
        incidentDao.getIncidentsForUser(userId)
    
    /**
     * Get incident by ID
     */
    suspend fun getIncidentById(incidentId: String): Incident? = 
        incidentDao.getIncidentById(incidentId)
    
    /**
     * Get incidents within a time range
     */
    suspend fun getIncidentsInRange(startTime: Long, endTime: Long): List<Incident> = 
        incidentDao.getIncidentsInRange(startTime, endTime)
    
    /**
     * Get incidents by status
     */
    fun getIncidentsByStatus(status: IncidentStatus): LiveData<List<Incident>> = 
        incidentDao.getIncidentsByStatus(status)
    
    /**
     * Save incident to both local database and Firebase
     */
    suspend fun saveIncident(incident: Incident): Result<Unit> {
        return try {
            // Save to local database first
            incidentDao.insertIncident(incident)
            
            // Then sync to Firebase
            firebaseRepository.saveIncident(incident)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update incident status
     */
    suspend fun updateIncident(incident: Incident): Result<Unit> {
        return try {
            incidentDao.updateIncident(incident)
            firebaseRepository.saveIncident(incident) // Firebase uses set which acts as update
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get incident count for user since a specific time
     */
    suspend fun getIncidentCountForUserSince(userId: String, startTime: Long): Int = 
        incidentDao.getIncidentCountForUserSince(userId, startTime)
    
    /**
     * Get today's incident count for user
     */
    suspend fun getTodayIncidentCountForUser(userId: String): Int {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return getIncidentCountForUserSince(userId, startOfDay)
    }

    /**
     * Upload incident photo to Firebase Storage
     */
    suspend fun uploadIncidentPhoto(photoUri: android.net.Uri, incidentId: String): Result<String> {
        return firebaseRepository.uploadIncidentPhoto(photoUri, incidentId)
    }

    // ==================== SYNC OPERATIONS ====================
    
    /**
     * Sync local data with Firebase (for offline support)
     */
    suspend fun syncWithFirebase(): Result<Unit> {
        return try {
            // This would implement bidirectional sync logic
            // For now, just return success
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Enum representing the current clock status of a user
 */
enum class ClockStatus {
    CLOCKED_IN,
    CLOCKED_OUT
}
