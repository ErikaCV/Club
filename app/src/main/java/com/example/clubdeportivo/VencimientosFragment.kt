package com.example.clubdeportivo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.clubdeportivo.data.CuotaMensualDao
import com.example.clubdeportivo.data.DBContract
import com.example.clubdeportivo.data.DBHelper
import java.util.Calendar

class VencimientosFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(R.layout.fragment_vencimientos, container, false)

    override fun onResume() {
        super.onResume()

        // 1) Referenciamos el contenedor donde vamos a agregar filas dinámicas
        val cont = requireView().findViewById<LinearLayout>(R.id.listaClientes)
        cont.removeAllViews()

        // 2) Ejecutamos la consulta “vencen hoy” del DAO
        val cursor = CuotaMensualDao.vencenHoy(requireContext())
        val infl   = layoutInflater

        if (cursor.count == 0) {
            // 2️⃣  Si no hay resultados, mostramos el estado vacío
            val vacio = infl.inflate(R.layout.item_empty_state, cont, false)
            cont.addView(vacio)
        } else {
            // 3️⃣  Hay socios que vencen hoy → creamos una fila por cada uno
            while (cursor.moveToNext()) {
                val fila = infl.inflate(R.layout.item_cliente, cont, false)

                // Nombre del socio
                fila.findViewById<TextView>(R.id.tvNombre).text =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow(DBContract.ClienteEntry.COL_NOMBRE)
                    )

                // DNI del socio
                fila.findViewById<TextView>(R.id.tvDni).text =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow(DBContract.ClienteEntry.COL_DNI)
                    )

                // Fecha de vencimiento (alias "fechaVto" en la consulta SQL)
                fila.findViewById<TextView>(R.id.tvTipo).text =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow("fechaVto")
                    )

                cont.addView(fila)
            }
        }

        cursor.close()
    }
}