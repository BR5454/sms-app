package com.example.smsforwarder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    
    private val PERMISSIONS = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.INTERNET
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val serverUrlInput = findViewById<EditText>(R.id.serverUrl)
        val saveButton = findViewById<Button>(R.id.saveButton)

        // Load saved URL
        val prefs = getSharedPreferences("SMSForwarder", MODE_PRIVATE)
        serverUrlInput.setText(prefs.getString("server_url", "http://YOUR_PC_IP:3000"))

        saveButton.setOnClickListener {
            val url = serverUrlInput.text.toString()
            prefs.edit().putString("server_url", url).apply()
            Toast.makeText(this, "Server URL saved!", Toast.LENGTH_SHORT).show()
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        val missing = PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
        }
    }
}