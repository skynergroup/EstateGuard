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
        
        // Mock users for testing
        private val MOCK_USERS = listOf(
            User(
                id = "admin_001",
                email = "admin@estateguard.com",
                name = "John Smith",
                role = UserRole.ADMIN
            ),
            User(
                id = "guard_001",
                email = "guard@estateguard.com",
                name = "Michael Johnson",
                role = UserRole.SECURITY_GUARD
            )
        )
    }
    
    fun login(email: String, password: String): AuthResult {
        // Mock authentication - in real app, this would call backend API
        val user = MOCK_USERS.find { it.email == email }
        
        return if (user != null && password == "password123") {
            // Save user session
            prefs.edit().apply {
                putString(KEY_USER_ID, user.id)
                putString(KEY_USER_EMAIL, user.email)
                putString(KEY_USER_NAME, user.name)
                putString(KEY_USER_ROLE, user.role.name)
                putBoolean(KEY_IS_LOGGED_IN, true)
                apply()
            }
            AuthResult.Success(user)
        } else {
            AuthResult.Error("Invalid email or password")
        }
    }
    
    fun logout() {
        prefs.edit().clear().apply()
    }
    
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
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

    // Method for testing - simulate login with mock users
    fun simulateLogin(userType: String = "guard"): AuthResult {
        val user = if (userType == "admin") {
            MOCK_USERS.find { it.role == UserRole.ADMIN }
        } else {
            MOCK_USERS.find { it.role == UserRole.SECURITY_GUARD }
        }

        return if (user != null) {
            // Save user session
            prefs.edit().apply {
                putString(KEY_USER_ID, user.id)
                putString(KEY_USER_EMAIL, user.email)
                putString(KEY_USER_NAME, user.name)
                putString(KEY_USER_ROLE, user.role.name)
                putBoolean(KEY_IS_LOGGED_IN, true)
                apply()
            }
            AuthResult.Success(user)
        } else {
            AuthResult.Error("User not found")
        }
    }
}

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
