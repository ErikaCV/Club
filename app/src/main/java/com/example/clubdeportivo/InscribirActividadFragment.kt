package com.example.clubdeportivo

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.clubdeportivo.data.ActividadDao
import com.example.clubdeportivo.data.DBContract
import com.example.clubdeportivo.data.InscripcionDao
import com.example.clubdeportivo.data.PagoDiaDao

class InscribirActividadFragment : Fragment() {

    /* UI */
    private lateinit var etDni        : EditText
    private lateinit var spnActividad : Spinner
    private lateinit var spnHorario   : Spinner
    private lateinit var btnInscribir : Button

    /* selección */
    private var actividadSeleccionada = ""

    /* horarios fijos */
    private val horarios = arrayOf("12–14", "16–18", "20–22")

    /*──────────────────────────────────────────────────────────────*/
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        i.inflate(R.layout.fragment_inscribir_actividad, c, false)

    override fun onViewCreated(v: View, s: Bundle?) = with(v) {
        super.onViewCreated(v, s)

        etDni        = findViewById(R.id.editTextDni)
        spnActividad = findViewById(R.id.spinnerActividad)
        spnHorario   = findViewById(R.id.spinnerHorario)
        btnInscribir = findViewById(R.id.btnInscribirse)

        poblarSpinnerActividad()
        poblarSpinnerHorario()

        btnInscribir.setOnClickListener { inscribir() }
    }

    /*─── Spinner ACTIVIDAD (solo nombre) ─────────────────────────*/
    private fun poblarSpinnerActividad() {
        val cur = ActividadDao.todas(requireContext())
        val nombres = mutableListOf<String>()
        while (cur.moveToNext()) {
            nombres += cur.getString(
                cur.getColumnIndexOrThrow(DBContract.ActividadEntry.COL_NOMBRE)
            )
        }
        cur.close()

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            nombres
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnActividad.adapter = adapter

        if (nombres.isNotEmpty()) {
            actividadSeleccionada = nombres[0]
        }

        spnActividad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: View?, pos: Int, id: Long) {
                actividadSeleccionada = nombres[pos]
            }
            override fun onNothingSelected(p0: AdapterView<*>?) { /* no-op */ }
        }
    }

    private fun poblarSpinnerHorario() {
        val adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_item, horarios)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnHorario.adapter = adapter
    }

    private fun inscribir() {
        val dni     = etDni.text.toString().trim()
        val horario = spnHorario.selectedItem.toString()

        if (dni.isEmpty()) { toast("Ingrese DNI"); return }


        if (!PagoDiaDao.yaPagoHoy(requireContext(), dni)) {
            toast("Debe pagar la cuota diaria antes de inscribirse")
            return
        }


        val ok = InscripcionDao.inscribir(requireContext(), dni,
            actividadSeleccionada, horario)

        if (ok) {
            toast("Inscripción registrada")
            parentFragmentManager.popBackStack()
        } else {
            toast("Ya estabas inscripto en ese horario")
        }
    }

    private fun toast(m: String) =
        Toast.makeText(requireContext(), m, Toast.LENGTH_SHORT).show()
}