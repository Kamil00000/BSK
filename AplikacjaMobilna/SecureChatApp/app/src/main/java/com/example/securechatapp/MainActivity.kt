package com.example.securechatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.securechatapp.ui.theme.SecureChatAppTheme

import android.content.Intent
import android.widget.Button
import android.widget.Toast
import com.example.securechatapp.model.AuthPrefs
import com.example.securechatapp.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val logoutButton = findViewById<Button>(R.id.btnLogout)
        logoutButton.setOnClickListener {
            val prefs = AuthPrefs(this)
            val token = prefs.authToken

            if (token != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = ApiClient.instance.logout(token)

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                // Usuwamy tokeny lokalnie
                                prefs.authToken = null
                                prefs.refreshToken = null

                                // Przechodzimy do ekranu logowania
                                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@MainActivity, "Błąd podczas wylogowywania", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Błąd sieci: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Brak tokenu", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
