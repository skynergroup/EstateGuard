package za.co.skyner.estateguard.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import za.co.skyner.estateguard.data.model.User

@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE isActive = 1")
    fun getAllActiveUsers(): LiveData<List<User>>
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("UPDATE users SET isActive = 0 WHERE id = :userId")
    suspend fun deactivateUser(userId: String)
}
