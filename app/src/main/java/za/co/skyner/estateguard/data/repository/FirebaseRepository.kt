package za.co.skyner.estateguard.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import za.co.skyner.estateguard.data.model.Incident
import za.co.skyner.estateguard.data.model.TimeEntry
import za.co.skyner.estateguard.data.model.User
import java.util.*

class FirebaseRepository {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val TIME_ENTRIES_COLLECTION = "time_entries"
        private const val INCIDENTS_COLLECTION = "incidents"
        private const val INCIDENT_PHOTOS_PATH = "incident_photos"
    }
    
    // User operations
    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUser(userId: String): Result<User?> {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            val user = if (document.exists()) {
                document.toObject(User::class.java)
            } else {
                null
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Time Entry operations
    suspend fun saveTimeEntry(timeEntry: TimeEntry): Result<Unit> {
        return try {
            firestore.collection(TIME_ENTRIES_COLLECTION)
                .document(timeEntry.id)
                .set(timeEntry)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getTimeEntriesForUser(userId: String): Flow<List<TimeEntry>> = flow {
        try {
            val snapshot = firestore.collection(TIME_ENTRIES_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val timeEntries = snapshot.documents.mapNotNull { document ->
                document.toObject(TimeEntry::class.java)
            }
            emit(timeEntries)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    suspend fun getLastTimeEntryForUser(userId: String): Result<TimeEntry?> {
        return try {
            val snapshot = firestore.collection(TIME_ENTRIES_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            
            val timeEntry = snapshot.documents.firstOrNull()?.toObject(TimeEntry::class.java)
            Result.success(timeEntry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Incident operations
    suspend fun saveIncident(incident: Incident): Result<Unit> {
        return try {
            firestore.collection(INCIDENTS_COLLECTION)
                .document(incident.id)
                .set(incident)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getIncidentsForUser(userId: String): Flow<List<Incident>> = flow {
        try {
            val snapshot = firestore.collection(INCIDENTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val incidents = snapshot.documents.mapNotNull { document ->
                document.toObject(Incident::class.java)
            }
            emit(incidents)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    fun getAllIncidents(): Flow<List<Incident>> = flow {
        try {
            val snapshot = firestore.collection(INCIDENTS_COLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val incidents = snapshot.documents.mapNotNull { document ->
                document.toObject(Incident::class.java)
            }
            emit(incidents)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    // Photo upload for incidents
    suspend fun uploadIncidentPhoto(imageUri: Uri, incidentId: String): Result<String> {
        return try {
            val fileName = "incident_${incidentId}_${System.currentTimeMillis()}.jpg"
            val photoRef = storage.reference.child("$INCIDENT_PHOTOS_PATH/$fileName")
            
            val uploadTask = photoRef.putFile(imageUri).await()
            val downloadUrl = photoRef.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Statistics and reporting
    suspend fun getIncidentCountForUserToday(userId: String): Result<Int> {
        return try {
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            val snapshot = firestore.collection(INCIDENTS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .get()
                .await()
            
            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTotalIncidentsCount(): Result<Int> {
        return try {
            val snapshot = firestore.collection(INCIDENTS_COLLECTION).get().await()
            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTotalUsersCount(): Result<Int> {
        return try {
            val snapshot = firestore.collection(USERS_COLLECTION)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
