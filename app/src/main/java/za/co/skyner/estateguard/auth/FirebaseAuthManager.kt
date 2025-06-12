package za.co.skyner.estateguard.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import za.co.skyner.estateguard.data.model.User
import za.co.skyner.estateguard.data.model.UserRole

class FirebaseAuthManager(private val context: Context) {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    companion object {
        private const val USERS_COLLECTION = "users"
    }
    
    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                val user = getUserFromFirestore(firebaseUser.uid)
                if (user != null) {
                    AuthResult.Success(user)
                } else {
                    AuthResult.Error("User profile not found")
                }
            } else {
                AuthResult.Error("Authentication failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    suspend fun signUp(email: String, password: String, name: String, role: UserRole): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid,
                    email = email,
                    name = name,
                    role = role,
                    isActive = true,
                    createdAt = System.currentTimeMillis()
                )
                
                // Save user profile to Firestore
                saveUserToFirestore(user)
                AuthResult.Success(user)
            } else {
                AuthResult.Error("User creation failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    fun signOut() {
        auth.signOut()
    }
    
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    suspend fun getCurrentUser(): User? {
        val firebaseUser = getCurrentFirebaseUser()
        return if (firebaseUser != null) {
            getUserFromFirestore(firebaseUser.uid)
        } else {
            null
        }
    }
    
    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    
    private suspend fun getUserFromFirestore(userId: String): User? {
        return try {
            val document = firestore.collection(USERS_COLLECTION).document(userId).get().await()
            if (document.exists()) {
                document.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun saveUserToFirestore(user: User) {
        try {
            firestore.collection(USERS_COLLECTION).document(user.id).set(user).await()
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    // Create demo users for testing
    suspend fun createDemoUsers() {
        try {
            // Create admin user
            val adminResult = signUp(
                "admin@estateguard.com",
                "password123",
                "John Smith",
                UserRole.ADMIN
            )
            
            // Create guard user
            val guardResult = signUp(
                "guard@estateguard.com", 
                "password123",
                "Michael Johnson",
                UserRole.SECURITY_GUARD
            )
            
            // Sign out after creating demo users
            signOut()
        } catch (e: Exception) {
            // Demo users might already exist
        }
    }
}
