package za.co.skyner.estateguard.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.messaging.FirebaseMessaging
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
    private val messaging = FirebaseMessaging.getInstance()

    companion object {
        private const val TAG = "FirebaseRepository"
        private const val USERS_COLLECTION = "users"
        private const val TIME_ENTRIES_COLLECTION = "timeEntries"
        private const val INCIDENTS_COLLECTION = "incidents"
        private const val ESTATES_COLLECTION = "estates"
        private const val SETTINGS_COLLECTION = "settings"
        private const val AUDIT_LOGS_COLLECTION = "auditLogs"
        private const val REPORTS_COLLECTION = "reports"
        private const val INCIDENT_PHOTOS_PATH = "incident_photos"
        private const val PROFILE_PHOTOS_PATH = "profile_photos"
        private const val QR_CODES_PATH = "qr_codes"
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

    // ==================== ESTATE OPERATIONS ====================

    suspend fun getEstateById(estateId: String): Result<Map<String, Any>?> {
        return try {
            val document = firestore.collection(ESTATES_COLLECTION)
                .document(estateId)
                .get()
                .await()
            Result.success(document.data)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting estate", e)
            Result.failure(e)
        }
    }

    suspend fun getAllEstates(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection(ESTATES_COLLECTION)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            val estates = snapshot.documents.mapNotNull { it.data }
            Result.success(estates)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting estates", e)
            Result.failure(e)
        }
    }

    // ==================== SETTINGS OPERATIONS ====================

    suspend fun getAppSettings(): Result<Map<String, Any>?> {
        return try {
            val document = firestore.collection(SETTINGS_COLLECTION)
                .document("app_config")
                .get()
                .await()
            Result.success(document.data)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app settings", e)
            Result.failure(e)
        }
    }

    suspend fun updateAppSettings(settings: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection(SETTINGS_COLLECTION)
                .document("app_config")
                .update(settings + mapOf("updatedAt" to FieldValue.serverTimestamp()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating app settings", e)
            Result.failure(e)
        }
    }

    // ==================== PUSH NOTIFICATION OPERATIONS ====================

    suspend fun updateUserFCMToken(userId: String, token: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update(mapOf(
                    "fcmToken" to token,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating FCM token", e)
            Result.failure(e)
        }
    }

    suspend fun sendNotificationToAdmins(title: String, body: String, data: Map<String, String> = emptyMap()): Result<Unit> {
        return try {
            // This would typically be handled by Cloud Functions
            // For now, we'll just log it
            Log.d(TAG, "Notification to admins: $title - $body")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification", e)
            Result.failure(e)
        }
    }

    // ==================== AUDIT LOG OPERATIONS ====================

    suspend fun logAuditEvent(action: String, userId: String, resourceId: String?, metadata: Map<String, Any> = emptyMap()): Result<Unit> {
        return try {
            val auditLog = mapOf(
                "action" to action,
                "userId" to userId,
                "resourceId" to resourceId,
                "metadata" to metadata,
                "timestamp" to FieldValue.serverTimestamp()
            )

            firestore.collection(AUDIT_LOGS_COLLECTION)
                .add(auditLog)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error logging audit event", e)
            Result.failure(e)
        }
    }

    // ==================== PROFILE PHOTO OPERATIONS ====================

    suspend fun uploadProfilePhoto(photoUri: Uri, userId: String): Result<String> {
        return try {
            val fileName = "profile_${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference
                .child(PROFILE_PHOTOS_PATH)
                .child(userId)
                .child(fileName)

            val uploadTask = storageRef.putFile(photoUri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()

            // Update user profile with photo URL
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .update(mapOf(
                    "profilePhotoUrl" to downloadUrl.toString(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
                .await()

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile photo", e)
            Result.failure(e)
        }
    }

    // ==================== BATCH OPERATIONS ====================

    suspend fun batchUpdateIncidentStatus(incidentIds: List<String>, newStatus: String): Result<Unit> {
        return try {
            val batch = firestore.batch()

            incidentIds.forEach { incidentId ->
                val docRef = firestore.collection(INCIDENTS_COLLECTION).document(incidentId)
                batch.update(docRef, mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error batch updating incidents", e)
            Result.failure(e)
        }
    }

    // ==================== ANALYTICS OPERATIONS ====================

    suspend fun getIncidentStatsByEstate(estateId: String, startDate: Long, endDate: Long): Result<Map<String, Int>> {
        return try {
            val snapshot = firestore.collection(INCIDENTS_COLLECTION)
                .whereEqualTo("estateId", estateId)
                .whereGreaterThanOrEqualTo("timestamp", startDate)
                .whereLessThanOrEqualTo("timestamp", endDate)
                .get()
                .await()

            val stats = mutableMapOf<String, Int>()
            snapshot.documents.forEach { doc ->
                val severity = doc.getString("severity") ?: "UNKNOWN"
                stats[severity] = stats.getOrDefault(severity, 0) + 1
            }

            Result.success(stats.toMap())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting incident stats", e)
            Result.failure(e)
        }
    }
}
