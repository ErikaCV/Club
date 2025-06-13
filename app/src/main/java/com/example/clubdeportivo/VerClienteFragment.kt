package com.example.clubdeportivo


import android.database.Cursor
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.clubdeportivo.data.ClienteDao
import com.example.clubdeportivo.data.DBContract

/**
 * Muestra la lista de clientes en la pantalla "Clientes".
 */
class VerClienteFragment : Fragment() {

    /* referencias */
    private lateinit var contenedor : LinearLayout        // listaClientes
    private lateinit var etFiltro  : EditText             // etBuscarDNI
    private lateinit var btnBuscar : ImageButton          // btnBuscar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_ver_cliente, container, false)

    override fun onViewCreated(v: View, s: Bundle?) = with(v) {
        super.onViewCreated(v, s)
        contenedor = findViewById(R.id.listaClientes)
        etFiltro   = findViewById(R.id.etBuscarDNI)
        btnBuscar  = findViewById(R.id.btnBuscar)

        btnBuscar.setOnClickListener { cargarLista() }

        /* al arrancar muestra todo */
        cargarLista()
    }

    override fun onResume() {
        super.onResume()
        cargarLista()          // refresco al volver de alta/edición
    }

    /*─────────────────────────── núcleo ──────────────────────────*/
    private fun cargarLista() {
        contenedor.removeAllViews()

        val dni = etFiltro.text.toString().trim()
        val cursor: Cursor = if (dni.isEmpty())
            ClienteDao.buscarTodos(requireContext())
        else
            ClienteDao.buscarPorDni(requireContext(), dni)

        val infl = layoutInflater
        while (cursor.moveToNext()) {
            val fila = infl.inflate(R.layout.item_cliente, contenedor, false)

            fila.findViewById<TextView>(R.id.tvNombre).text =
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.ClienteEntry.COL_NOMBRE))

            fila.findViewById<TextView>(R.id.tvDni).text =
                cursor.getString(cursor.getColumnIndexOrThrow(DBContract.ClienteEntry.COL_DNI))

            /*  elimina el guion bajo  */
            val tipo = cursor.getString(cursor.getColumnIndexOrThrow(DBContract.ClienteEntry.COL_TIPO))
                .replace('_',' ')        //  "NO_SOCIO" -> "NO SOCIO"
            fila.findViewById<TextView>(R.id.tvTipo).text = tipo

            contenedor.addView(fila)
        }
        cursor.close()
    }
}


