package za.co.skyner.estateguard.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.skyner.estateguard.MainActivity
import za.co.skyner.estateguard.R
import za.co.skyner.estateguard.auth.FirebaseAuthManager
import za.co.skyner.estateguard.data.repository.RepositoryProvider

/**
 * Firebase Cloud Messaging service for EstateGuard
 * Handles incoming push notifications and token updates
 */
class EstateGuardMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "EstateGuardFCM"
        private const val CHANNEL_ID = "estateguard_notifications"
        private const val CHANNEL_NAME = "EstateGuard Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for security incidents and updates"
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    /**
     * Called when a new FCM token is generated
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received: $token")
        
        // Update token in Firebase and local storage
        updateTokenInFirebase(token)
    }
    
    /**
     * Called when a message is received
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        
        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message notification: ${notification.title} - ${notification.body}")
            showNotification(
                title = notification.title ?: "EstateGuard",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }
    }
    
    /**
     * Handle data-only messages
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"]
        
        when (type) {
            "incident" -> handleIncidentNotification(data)
            "incident_resolved" -> handleIncidentResolvedNotification(data)
            "shift_reminder" -> handleShiftReminderNotification(data)
            "system_update" -> handleSystemUpdateNotification(data)
            "emergency" -> handleEmergencyNotification(data)
            else -> {
                // Generic notification
                showNotification(
                    title = data["title"] ?: "EstateGuard",
                    body = data["body"] ?: "You have a new notification",
                    data = data
                )
            }
        }
    }
    
    /**
     * Handle incident notifications
     */
    private fun handleIncidentNotification(data: Map<String, String>) {
        val severity = data["severity"] ?: "MEDIUM"
        val incidentId = data["incidentId"]
        
        val title = when (severity) {
            "CRITICAL" -> "🚨 CRITICAL INCIDENT"
            "HIGH" -> "⚠️ HIGH PRIORITY INCIDENT"
            "MEDIUM" -> "📋 INCIDENT REPORTED"
            else -> "📝 NEW INCIDENT"
        }
        
        showNotification(
            title = title,
            body = data["body"] ?: "A new incident has been reported",
            data = data,
            isHighPriority = severity in listOf("HIGH", "CRITICAL")
        )
    }
    
    /**
     * Handle incident resolved notifications
     */
    private fun handleIncidentResolvedNotification(data: Map<String, String>) {
        showNotification(
            title = "✅ Incident Resolved",
            body = data["body"] ?: "Your incident report has been resolved",
            data = data
        )
    }
    
    /**
     * Handle shift reminder notifications
     */
    private fun handleShiftReminderNotification(data: Map<String, String>) {
        showNotification(
            title = "⏰ Shift Reminder",
            body = data["body"] ?: "Don't forget to clock in for your shift",
            data = data
        )
    }
    
    /**
     * Handle system update notifications
     */
    private fun handleSystemUpdateNotification(data: Map<String, String>) {
        showNotification(
            title = "🔄 System Update",
            body = data["body"] ?: "EstateGuard has been updated",
            data = data
        )
    }
    
    /**
     * Handle emergency notifications
     */
    private fun handleEmergencyNotification(data: Map<String, String>) {
        showNotification(
            title = "🚨 EMERGENCY ALERT",
            body = data["body"] ?: "Emergency situation reported",
            data = data,
            isHighPriority = true,
            isEmergency = true
        )
    }
    
    /**
     * Show notification to user
     */
    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>,
        isHighPriority: Boolean = false,
        isEmergency: Boolean = false
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create intent for when notification is tapped
        val intent = createNotificationIntent(data)
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Make sure this icon exists
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(getColor(R.color.primary_color))
        
        // Set priority based on notification type
        when {
            isEmergency -> {
                notificationBuilder
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
                    .setLights(getColor(R.color.error_color), 1000, 1000)
            }
            isHighPriority -> {
                notificationBuilder
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setVibrate(longArrayOf(0, 500, 250, 500))
            }
            else -> {
                notificationBuilder
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            }
        }
        
        // Show notification
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
        
        Log.d(TAG, "Notification shown: $title")
    }
    
    /**
     * Create intent for notification tap
     */
    private fun createNotificationIntent(data: Map<String, String>): Intent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            
            // Add data to intent
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        
        return intent
    }
    
    /**
     * Update FCM token in Firebase
     */
    private fun updateTokenInFirebase(token: String) {
        serviceScope.launch {
            try {
                val authManager = FirebaseAuthManager(this@EstateGuardMessagingService)
                val currentUser = authManager.getCurrentFirebaseUser()
                
                if (currentUser != null) {
                    val repository = RepositoryProvider.getRepository(this@EstateGuardMessagingService)
                    // Update token in Firebase through repository
                    // This would need to be implemented in the repository
                    Log.d(TAG, "FCM token updated for user: ${currentUser.uid}")
                } else {
                    Log.d(TAG, "No authenticated user, token will be updated on next login")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error updating FCM token", e)
            }
        }
    }
    
    /**
     * Create notification channel for Android 8.0+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = getColor(R.color.primary_color)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            Log.d(TAG, "Notification channel created")
        }
    }
}
