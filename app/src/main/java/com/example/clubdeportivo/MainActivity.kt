package com.example.clubdeportivo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.clubdeportivo.data.AuthDao
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutLogin    = findViewById<LinearLayout>(R.id.llLogin)
        val layoutRegistro = findViewById<LinearLayout>(R.id.llRegistro)


        val etUser = findViewById<EditText>(R.id.name)
        val etPass = findViewById<EditText>(R.id.editTextTextPassword)
        val btnIngresar = findViewById<Button>(R.id.btnIngresar)

        findViewById<TextView>(R.id.tvRegistrarse).setOnClickListener {
            layoutLogin.visibility = View.GONE
            layoutRegistro.visibility = View.VISIBLE
        }

        findViewById<TextView>(R.id.tvVolverLogin).setOnClickListener {
            layoutRegistro.visibility = View.GONE
            layoutLogin.visibility = View.VISIBLE
        }


        btnIngresar.setOnClickListener {
            val user = etUser.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (AuthDao.login(this, user, pass)) {
                startActivity(Intent(this, PrincipalActivity::class.java))
                finish() // evita volver al login con "atrás"
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
