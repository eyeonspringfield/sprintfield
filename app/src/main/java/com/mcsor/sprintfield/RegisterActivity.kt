package com.mcsor.sprintfield

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity(){

    lateinit var registerButton: Button
    lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)

        registerButton.setOnClickListener{
            Toast.makeText(this, "Register clicked!!!!!!!!", Toast.LENGTH_SHORT).show();
        }

        loginButton.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}