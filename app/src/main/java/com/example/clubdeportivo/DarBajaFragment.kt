package com.example.clubdeportivo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.clubdeportivo.data.DBContract
import com.example.clubdeportivo.data.DBHelper

class DarBajaFragment : Fragment() {

    private lateinit var etDni: EditText
    private lateinit var panelConfirmacion: View
    private lateinit var txtConfirmacion: TextView
    private var dniIngresado: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_dar_baja, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etDni            = view.findViewById(R.id.editTextDni)
        panelConfirmacion = (view.findViewById<TextView>(R.id.textSaldo)).parent as View
        txtConfirmacion  = view.findViewById(R.id.textSaldo)

        panelConfirmacion.visibility = View.GONE

        view.findViewById<Button>(R.id.btnDarDeBaja).setOnClickListener {
            dniIngresado = etDni.text.toString().trim()
            if (dniIngresado.isEmpty()) {
                Toast.makeText(context, "Ingrese un DNI", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            txtConfirmacion.text = "¿Está seguro de borrar al cliente con DNI $dniIngresado?"
            panelConfirmacion.visibility = View.VISIBLE
        }

        view.findViewById<Button>(R.id.btnAceptar).setOnClickListener {
            borrarClientePorDni(dniIngresado)
        }

        view.findViewById<Button>(R.id.btnCerrar).setOnClickListener {
            panelConfirmacion.visibility = View.GONE
        }
    }

    private fun borrarClientePorDni(dni: String) {
        val db = DBHelper(requireContext()).writableDatabase
        val filas = db.delete(
            DBContract.ClienteEntry.TABLE,
            "${DBContract.ClienteEntry.COL_DNI} = ?",
            arrayOf(dni)
        )
        db.close()

        if (filas > 0) {
            Toast.makeText(context, "Cliente eliminado", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()  // volver a la pantalla anterior
        } else {
            Toast.makeText(context, "No se encontró cliente con ese DNI", Toast.LENGTH_SHORT).show()
        }
    }
}
