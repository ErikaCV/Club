package com.example.clubdeportivo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment

class InicioFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_inicio, container, false)


        view.findViewById<ImageButton>(R.id.btnInscribirCliente)
            .setOnClickListener { navigateTo(InscribirClienteFragment()) }

        view.findViewById<ImageButton>(R.id.btnDarBaja)
            .setOnClickListener { navigateTo(DarBajaFragment()) }

        view.findViewById<ImageButton>(R.id.btnVerClientes)
            .setOnClickListener { navigateTo(VerClienteFragment()) }

        view.findViewById<ImageButton>(R.id.btnVencimientos)
            .setOnClickListener { navigateTo(VencimientosFragment()) }

        view.findViewById<ImageButton>(R.id.btnInscribirActividad)
            .setOnClickListener { navigateTo(InscribirActividadFragment()) }

        view.findViewById<ImageButton>(R.id.btnVerInscriptos)
            .setOnClickListener { navigateTo(VerInscriptosFragment()) }

        view.findViewById<ImageButton>(R.id.btnPagarCuotaDiaria)
            .setOnClickListener { navigateTo(PagoCuotaDiariaFragment()) }

        view.findViewById<ImageButton>(R.id.btnPagarCuotaMensual)
            .setOnClickListener { navigateTo(PagoCuotaMensualFragment()) }

        return view
    }


    private fun navigateTo(fragment: Fragment) {

        parentFragmentManager.beginTransaction()
            .replace(R.id.contenedorPrincipal, fragment)
            .addToBackStack(null)
            .commit()
    }
}