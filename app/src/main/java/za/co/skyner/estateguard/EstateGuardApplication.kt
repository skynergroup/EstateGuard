package za.co.skyner.estateguard

import android.app.Application
import android.util.Log
import za.co.skyner.estateguard.firebase.FirebaseManager

/**
 * Application class for EstateGuard
 * Handles app-wide initialization and configuration
 */
class EstateGuardApplication : Application() {
    
    companion object {
        private const val TAG = "EstateGuardApp"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "EstateGuard Application starting...")
        
        // Initialize Firebase
        initializeFirebase()
        
        // Initialize other app components
        initializeAppComponents()
        
        Log.d(TAG, "EstateGuard Application initialized successfully")
    }
    
    /**
     * Initialize Firebase components
     */
    private fun initializeFirebase() {
        try {
            FirebaseManager.initialize(this)
            Log.d(TAG, "Firebase initialization completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase", e)
        }
    }
    
    /**
     * Initialize other app components
     */
    private fun initializeAppComponents() {
        try {
            // Initialize Room database
            // This will be done lazily when first accessed through RepositoryProvider
            
            // Initialize other components as needed
            Log.d(TAG, "App components initialization completed")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing app components", e)
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        
        // Cleanup Firebase resources
        FirebaseManager.cleanup()
        
        Log.d(TAG, "EstateGuard Application terminated")
    }
}
