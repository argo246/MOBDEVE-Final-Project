package com.mobdeve.s19.group2.mco2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextFirstName: TextInputEditText
    private lateinit var editTextLastName: TextInputEditText
    private lateinit var buttonRegister: Button
    private lateinit var textView: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth and SharedPreferences
        mAuth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Initialize UI components
        editTextEmail = findViewById(R.id.editTextRegisterUsername)
        editTextPassword = findViewById(R.id.editTextRegisterPassword)
        editTextFirstName = findViewById(R.id.editTextFirstName)
        editTextLastName = findViewById(R.id.editTextLastName)
        buttonRegister = findViewById(R.id.buttonRegister)
        textView = findViewById(R.id.loginNowTextview)

        // Navigate to login screen when clicking "Already have an account"
        textView.setOnClickListener {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Handle register button click
        buttonRegister.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val firstName = editTextFirstName.text.toString()
            val lastName = editTextLastName.text.toString()

            // Validate input
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter an email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter a password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
                Toast.makeText(this, "Please fill in your first and last name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create a new user with Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = mAuth.currentUser
                        if (user != null) {
                            // Save user ID to SharedPreferences
                            val editor = sharedPreferences.edit()
                            editor.putString("first_name", firstName)
                            editor.putString("last_name", lastName)
                            editor.apply()

                            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()

                            // Navigate to login screen
                            val intent = Intent(applicationContext, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "User creation failed.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorMessage = task.exception?.message ?: "Unknown error occurred."
                        Toast.makeText(this, "Registration failed: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun checkIfUserLoggedIn() {
        // Retrieve user ID from SharedPreferences
        val userId = sharedPreferences.getString("userId", null)
        if (userId != null) {
            Toast.makeText(this, "User already logged in with ID: $userId", Toast.LENGTH_SHORT).show()
            // Optionally, redirect the user to the main activity or dashboard
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if the user is already logged in
        checkIfUserLoggedIn()
    }
}