package za.co.skyner.estateguard.data.repository

import android.content.Context
import za.co.skyner.estateguard.data.database.EstateGuardDatabase

/**
 * Provides a single instance of EstateGuardRepository throughout the app.
 * This ensures we have a single source of truth for data operations.
 */
object RepositoryProvider {
    
    @Volatile
    private var INSTANCE: EstateGuardRepository? = null
    
    fun getRepository(context: Context): EstateGuardRepository {
        return INSTANCE ?: synchronized(this) {
            val database = EstateGuardDatabase.getDatabase(context)
            val firebaseRepository = FirebaseRepository()
            
            val instance = EstateGuardRepository(
                userDao = database.userDao(),
                timeEntryDao = database.timeEntryDao(),
                incidentDao = database.incidentDao(),
                firebaseRepository = firebaseRepository
            )
            INSTANCE = instance
            instance
        }
    }
}
