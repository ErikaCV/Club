package com.example.clubdeportivo

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.clubdeportivo.data.InscripcionDao


class VerInscriptosFragment : Fragment() {

    private lateinit var contenedor: LinearLayout   // <LinearLayout android:id="@+id/listaClientes" …>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_ver_inscriptos, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contenedor = view.findViewById(R.id.listaClientes)
        cargarTodos()
    }

    private fun cargarTodos() {
        contenedor.removeAllViews()

        val c = InscripcionDao.obtenerTodos(requireContext())
        val inflater = LayoutInflater.from(requireContext())

        var actActual = ""
        var filaIndex = 0

        while (c.moveToNext()) {
            val act = c.getString(c.getColumnIndexOrThrow("actividad"))

            /* ────────── Encabezado por actividad ────────── */
            if (act != actActual) {
                actActual = act

                // “Inscriptos en …”
                val head = inflater.inflate(
                    R.layout.item_header_actividad, contenedor, false
                ) as TextView
                head.text = "Inscriptos en $act"
                contenedor.addView(head)

                // Títulos Nombre | DNI | Fecha | Horario
                contenedor.addView(
                    inflater.inflate(R.layout.item_header_columnas, contenedor, false)
                )
            }

            /* ────────── Fila de datos ────────── */
            val fila = inflater.inflate(R.layout.item_inscripto, contenedor, false)

            // alternar color para mejor contraste
            if (filaIndex % 2 == 1)      // impares → tono más claro
                fila.setBackgroundColor(0xFF027D8B.toInt())
            filaIndex++

            fila.findViewById<TextView>(R.id.tvNombre).text =
                c.getString(c.getColumnIndexOrThrow("nombre"))
            fila.findViewById<TextView>(R.id.tvDni).text =
                c.getString(c.getColumnIndexOrThrow("dni"))
            fila.findViewById<TextView>(R.id.tvFecha).text =
                c.getString(c.getColumnIndexOrThrow("fecha"))
            fila.findViewById<TextView>(R.id.tvHorario).text =
                c.getString(c.getColumnIndexOrThrow("horario"))

            contenedor.addView(fila)
        }
        c.close()
    }
}