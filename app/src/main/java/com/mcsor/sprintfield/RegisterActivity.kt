package com.mcsor.sprintfield

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var passwordAgain: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        passwordAgain = findViewById(R.id.passwordAgain)

        registerButton.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()
            val passwordAgainText = passwordAgain.text.toString().trim()

            if (emailText.isEmpty() || passwordText.isEmpty() || passwordAgainText.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_out), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordText != passwordAgainText) {
                Toast.makeText(this, getString(R.string.pass_no_match), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordText.length < 6) {
                Toast.makeText(this, getString(R.string.small_password), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        if (firebaseUser != null) {
                            val userData = hashMapOf(
                                "uid" to firebaseUser.uid,
                                "email" to firebaseUser.email,
                                "createdAt" to System.currentTimeMillis()
                            )
                            firestore.collection("users")
                                .document(firebaseUser.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.reg_success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        getString(R.string.user_add_failed),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.reg_failed, task.exception?.localizedMessage),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
