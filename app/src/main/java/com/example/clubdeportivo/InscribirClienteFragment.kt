package com.example.clubdeportivo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.clubdeportivo.data.ClienteDao

class InscribirClienteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_inscribir_cliente, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val etNombre   = view.findViewById<EditText>(R.id.etNombre)
        val etDni      = view.findViewById<EditText>(R.id.etDNI)
        val etFnac     = view.findViewById<EditText>(R.id.etFechaNacimiento)
        val etDir      = view.findViewById<EditText>(R.id.etDireccion)
        val etEmail    = view.findViewById<EditText>(R.id.etEmail)
        val etCel      = view.findViewById<EditText>(R.id.etCelular)
        val chkSocio   = view.findViewById<CheckBox>(R.id.cbEsSocio)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardar)
        val btnCerrar  = view.findViewById<Button>(R.id.btnCerrar)


        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val dni    = etDni.text.toString().trim()
            if (nombre.isEmpty() || dni.isEmpty()) {
                toast("Nombre y DNI son obligatorios"); return@setOnClickListener
            }

            val ok = ClienteDao.insertar(
                ctx       = requireContext(),
                nombre    = nombre,
                dni       = dni,
                esSocio   = chkSocio.isChecked,
                email     = etEmail.text.toString().trim(),
                fnac      = etFnac.text.toString().trim(),
                direccion = etDir.text.toString().trim(),
                celular   = etCel.text.toString().trim()
            )

            toast(if (ok) "Cliente agregado" else "Error: DNI ya registrado")
            if (ok) parentFragmentManager.popBackStack()
        }

        btnCerrar.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}