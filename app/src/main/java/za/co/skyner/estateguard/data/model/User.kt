package za.co.skyner.estateguard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.SECURITY_GUARD,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", UserRole.SECURITY_GUARD, true, System.currentTimeMillis())
}

enum class UserRole {
    ADMIN,
    SECURITY_GUARD
}
