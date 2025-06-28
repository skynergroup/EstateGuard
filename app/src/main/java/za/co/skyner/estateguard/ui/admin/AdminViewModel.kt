package za.co.skyner.estateguard.ui.admin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import za.co.skyner.estateguard.data.model.User
import za.co.skyner.estateguard.data.model.Incident
import java.util.Calendar

class AdminViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _totalUsers = MutableLiveData<Int>()
    val totalUsers: LiveData<Int> = _totalUsers

    private val _totalIncidents = MutableLiveData<Int>()
    val totalIncidents: LiveData<Int> = _totalIncidents

    private val _activeGuards = MutableLiveData<Int>()
    val activeGuards: LiveData<Int> = _activeGuards

    private val _todayIncidents = MutableLiveData<Int>()
    val todayIncidents: LiveData<Int> = _todayIncidents

    private val _guardsOnDuty = MutableLiveData<List<User>>()
    val guardsOnDuty: LiveData<List<User>> = _guardsOnDuty

    private val _recentIncidents = MutableLiveData<List<Incident>>()
    val recentIncidents: LiveData<List<Incident>> = _recentIncidents

    companion object {
        private const val TAG = "AdminViewModel"
    }

    init {
        loadAdminData()
    }

    private fun loadAdminData() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Load real data from Firebase
                loadUsersData()
                loadIncidentsData()
                loadGuardsOnDuty()
                loadRecentIncidents()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading admin data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadUsersData() {
        try {
            val usersSnapshot = firestore.collection("users").get().await()
            _totalUsers.value = usersSnapshot.size()

            // Count guards currently on duty (this would need a clock-in collection)
            // For now, we'll simulate this
            _activeGuards.value = 0 // Will be updated by loadGuardsOnDuty()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading users data", e)
            _totalUsers.value = 0
            _activeGuards.value = 0
        }
    }

    private suspend fun loadIncidentsData() {
        try {
            val incidentsSnapshot = firestore.collection("incidents").get().await()
            _totalIncidents.value = incidentsSnapshot.size()

            // Count today's incidents
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            val todayStart = today.timeInMillis
            val todayEnd = todayStart + 24 * 60 * 60 * 1000 // 24 hours

            val todayIncidentsSnapshot = firestore.collection("incidents")
                .whereGreaterThanOrEqualTo("timestamp", todayStart)
                .whereLessThan("timestamp", todayEnd)
                .get().await()

            _todayIncidents.value = todayIncidentsSnapshot.size()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading incidents data", e)
            _totalIncidents.value = 0
            _todayIncidents.value = 0
        }
    }

    private suspend fun loadGuardsOnDuty() {
        try {
            // This would typically check a clock-in/clock-out collection
            // For now, we'll get all active guards
            val guardsSnapshot = firestore.collection("users")
                .whereEqualTo("role", "SECURITY_GUARD")
                .whereEqualTo("isActive", true)
                .get().await()

            val guards = guardsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(User::class.java)
            }

            _guardsOnDuty.value = guards
            _activeGuards.value = guards.size
        } catch (e: Exception) {
            Log.e(TAG, "Error loading guards on duty", e)
            _guardsOnDuty.value = emptyList()
            _activeGuards.value = 0
        }
    }

    private suspend fun loadRecentIncidents() {
        try {
            val recentIncidentsSnapshot = firestore.collection("incidents")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get().await()

            val incidents = recentIncidentsSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Incident::class.java)
            }

            _recentIncidents.value = incidents
        } catch (e: Exception) {
            Log.e(TAG, "Error loading recent incidents", e)
            _recentIncidents.value = emptyList()
        }
    }

    fun refreshData() {
        loadAdminData()
    }
}
