package za.co.skyner.estateguard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import za.co.skyner.estateguard.auth.AuthResult
import za.co.skyner.estateguard.auth.FirebaseAuthManager
import za.co.skyner.estateguard.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuthManager: FirebaseAuthManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        firebaseAuthManager = FirebaseAuthManager(this)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.buttonLogin.setOnClickListener {
            performLogin()
        }

        binding.textForgotPassword.setOnClickListener {
            Toast.makeText(this, "Password reset feature coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.textHelp.setOnClickListener {
            Toast.makeText(this, "Support contact: admin@estateguard.com", Toast.LENGTH_LONG).show()
        }

        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Biometric authentication will be available in future updates", Toast.LENGTH_SHORT).show()
                binding.switchBiometric.isChecked = false
            }
        }
    }
    
    private fun performLogin() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        
        if (email.isEmpty()) {
            binding.editTextEmail.error = "Email is required"
            return
        }
        
        if (password.isEmpty()) {
            binding.editTextPassword.error = "Password is required"
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "Please enter a valid email"
            return
        }
        
        showLoading(true)
        
        lifecycleScope.launch {
            when (val result = firebaseAuthManager.signIn(email, password)) {
                is AuthResult.Success -> {
                    showLoading(false)
                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()

                    // Navigate to MainActivity
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is AuthResult.Error -> {
                    showLoading(false)
                    Toast.makeText(this@LoginActivity, "Login failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressContainer.visibility = if (show) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !show
        binding.editTextEmail.isEnabled = !show
        binding.editTextPassword.isEnabled = !show
    }
}
