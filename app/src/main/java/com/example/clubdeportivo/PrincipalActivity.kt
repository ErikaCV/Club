package com.example.clubdeportivo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView

class PrincipalActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnBack: ImageButton
    private lateinit var btnMenuToggle: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        drawerLayout   = findViewById(R.id.drawerLayout)
        btnBack        = findViewById(R.id.btnBack)
        btnMenuToggle  = findViewById(R.id.btnMenuToggle)
        val navigationView: NavigationView = findViewById(R.id.navigationView)

        btnMenuToggle.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.contenedorPrincipal, InicioFragment())
                .commit()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            updateBackButtonVisibility()
        }
        updateBackButtonVisibility()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            Log.d("MENU", "Clic en item: ${menuItem.itemId} / ${menuItem.title}")

            when (menuItem.itemId) {

                R.id.nav_cerrar_sesion -> {


                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    true
                }

                else -> {
                    val fragment: Fragment = when (menuItem.itemId) {
                        R.id.nav_inicio              -> InicioFragment()
                        R.id.nav_inscribir_cliente   -> InscribirClienteFragment()
                        R.id.nav_dar_baja            -> DarBajaFragment()
                        R.id.nav_vencimientos        -> VencimientosFragment()
                        R.id.nav_ver_cliente         -> VerClienteFragment()
                        R.id.nav_inscribir_actividad -> InscribirActividadFragment()
                        R.id.nav_ver_inscriptos      -> VerInscriptosFragment()
                        R.id.nav_pago_cuota_mensual  -> PagoCuotaMensualFragment()
                        R.id.nav_pago_cuota_diaria   -> PagoCuotaDiariaFragment()
                        else                         -> InicioFragment()
                    }

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.contenedorPrincipal, fragment)
                        .addToBackStack(null)
                        .commit()

                    drawerLayout.closeDrawers()
                    true
                }
            }
        }
    }

    private fun updateBackButtonVisibility() {
        btnBack.visibility =
            if (supportFragmentManager.backStackEntryCount > 0) View.VISIBLE
            else View.INVISIBLE
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
