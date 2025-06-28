package za.co.skyner.estateguard.auth

import android.content.Context
import android.content.SharedPreferences
import za.co.skyner.estateguard.data.model.User
import za.co.skyner.estateguard.data.model.UserRole
import java.util.UUID

class AuthManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGOUT_REQUESTED = "logout_requested"
        

    }
    
    fun saveUserSession(user: User) {
        prefs.edit().apply {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_ROLE, user.role.name)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    fun logout() {
        prefs.edit().apply {
            clear()
            putBoolean(KEY_LOGOUT_REQUESTED, true)
            apply()
        }
    }
    
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun wasLogoutRequested(): Boolean {
        return prefs.getBoolean(KEY_LOGOUT_REQUESTED, false)
    }

    fun clearLogoutRequest() {
        prefs.edit().putBoolean(KEY_LOGOUT_REQUESTED, false).apply()
    }
    
    fun getCurrentUser(): User? {
        if (!isLoggedIn()) return null
        
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val email = prefs.getString(KEY_USER_EMAIL, null) ?: return null
        val name = prefs.getString(KEY_USER_NAME, null) ?: return null
        val roleString = prefs.getString(KEY_USER_ROLE, null) ?: return null
        
        return try {
            User(
                id = userId,
                email = email,
                name = name,
                role = UserRole.valueOf(roleString)
            )
        } catch (e: Exception) {
            null
        }
    }
    
    fun getCurrentUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }


}

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
