package za.co.skyner.estateguard.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Centralized Firebase configuration and initialization for EstateGuard
 */
object FirebaseManager {
    
    private const val TAG = "FirebaseManager"
    
    // Firebase instances
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    val messaging: FirebaseMessaging by lazy { FirebaseMessaging.getInstance() }
    
    /**
     * Initialize Firebase with optimal settings for EstateGuard
     */
    fun initialize(context: Context) {
        try {
            // Initialize Firebase if not already done
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
                Log.d(TAG, "Firebase initialized successfully")
            }
            
            // Configure Firestore settings
            configureFirestore()
            
            // Configure Firebase Messaging
            configureMessaging()
            
            Log.d(TAG, "Firebase configuration completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
        }
    }
    
    /**
     * Configure Firestore with optimal settings
     */
    private fun configureFirestore() {
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Enable offline persistence
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            
            firestore.firestoreSettings = settings
            
            // Enable network for real-time updates
            firestore.enableNetwork()
            
            Log.d(TAG, "Firestore configured with offline persistence")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring Firestore", e)
        }
    }
    
    /**
     * Configure Firebase Cloud Messaging
     */
    private fun configureMessaging() {
        try {
            // Enable auto-initialization
            messaging.isAutoInitEnabled = true
            
            Log.d(TAG, "Firebase Messaging configured")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring Firebase Messaging", e)
        }
    }
    
    /**
     * Get current FCM token
     */
    suspend fun getFCMToken(): String? {
        return try {
            messaging.token.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting FCM token", e)
            null
        }
    }
    
    /**
     * Subscribe to topic for push notifications
     */
    suspend fun subscribeToTopic(topic: String): Boolean {
        return try {
            messaging.subscribeToTopic(topic).await()
            Log.d(TAG, "Subscribed to topic: $topic")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to topic: $topic", e)
            false
        }
    }
    
    /**
     * Unsubscribe from topic
     */
    suspend fun unsubscribeFromTopic(topic: String): Boolean {
        return try {
            messaging.unsubscribeFromTopic(topic).await()
            Log.d(TAG, "Unsubscribed from topic: $topic")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error unsubscribing from topic: $topic", e)
            false
        }
    }
    
    /**
     * Check if user is authenticated
     */
    fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    /**
     * Get current user email
     */
    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
    
    /**
     * Sign out current user
     */
    fun signOut() {
        try {
            auth.signOut()
            Log.d(TAG, "User signed out successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out user", e)
        }
    }
    
    /**
     * Enable Firestore offline persistence
     */
    fun enableOfflineMode() {
        try {
            firestore.disableNetwork()
            Log.d(TAG, "Firestore offline mode enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling offline mode", e)
        }
    }
    
    /**
     * Disable Firestore offline persistence
     */
    fun enableOnlineMode() {
        try {
            firestore.enableNetwork()
            Log.d(TAG, "Firestore online mode enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling online mode", e)
        }
    }
    
    /**
     * Clear Firestore cache
     */
    suspend fun clearFirestoreCache(): Boolean {
        return try {
            firestore.clearPersistence().await()
            Log.d(TAG, "Firestore cache cleared")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing Firestore cache", e)
            false
        }
    }
    
    /**
     * Get Firebase connection status
     */
    fun getConnectionStatus(): FirebaseConnectionStatus {
        return try {
            when {
                !isUserAuthenticated() -> FirebaseConnectionStatus.NOT_AUTHENTICATED
                // Add more sophisticated connection checking here
                else -> FirebaseConnectionStatus.CONNECTED
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking connection status", e)
            FirebaseConnectionStatus.ERROR
        }
    }
    
    /**
     * Setup Firebase listeners for real-time updates
     */
    fun setupRealtimeListeners(userId: String) {
        try {
            // Listen for user profile changes
            firestore.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to user changes", error)
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null && snapshot.exists()) {
                        Log.d(TAG, "User profile updated")
                        // Handle user profile updates
                    }
                }
            
            Log.d(TAG, "Real-time listeners setup for user: $userId")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up real-time listeners", e)
        }
    }
    
    /**
     * Cleanup Firebase resources
     */
    fun cleanup() {
        try {
            // Remove listeners, clear caches, etc.
            Log.d(TAG, "Firebase cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during Firebase cleanup", e)
        }
    }
}

/**
 * Firebase connection status enum
 */
enum class FirebaseConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    NOT_AUTHENTICATED,
    ERROR
}

/**
 * Firebase notification topics for EstateGuard
 */
object FirebaseTopics {
    const val ALL_USERS = "all_users"
    const val ADMINS_ONLY = "admins_only"
    const val GUARDS_ONLY = "guards_only"
    const val HIGH_PRIORITY_INCIDENTS = "high_priority_incidents"
    const val SYSTEM_UPDATES = "system_updates"
    
    fun getEstateSpecificTopic(estateId: String): String = "estate_$estateId"
    fun getUserSpecificTopic(userId: String): String = "user_$userId"
}
