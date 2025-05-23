package com.example.clubdeportivo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutLogin    = findViewById<LinearLayout>(R.id.llLogin)
        val layoutRegistro = findViewById<LinearLayout>(R.id.llRegistro)

        findViewById<TextView>(R.id.tvRegistrarse).setOnClickListener {
            layoutLogin.visibility = View.GONE
            layoutRegistro.visibility = View.VISIBLE
        }

        findViewById<TextView>(R.id.tvVolverLogin).setOnClickListener {
            layoutRegistro.visibility = View.GONE
            layoutLogin.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.btnIngresar).setOnClickListener {
            startActivity(Intent(this, PrincipalActivity::class.java))
            finish()
        }
    }
}
