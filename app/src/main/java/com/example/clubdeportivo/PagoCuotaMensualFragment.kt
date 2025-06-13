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

class PagoCuotaMensualFragment : Fragment() {

    private lateinit var etDni: EditText
    private lateinit var txtSaldo: TextView
    private lateinit var btnPagar: Button
    private lateinit var btnImprimir: Button
    private lateinit var btnConsultar: Button

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, b: Bundle?) =
        inflater.inflate(R.layout.fragment_pago_cuota_mensual, c, false)

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        etDni        = v.findViewById(R.id.editTextDni)
        txtSaldo     = v.findViewById(R.id.textSaldo)
        btnPagar     = v.findViewById(R.id.btnPagar)
        btnImprimir  = v.findViewById(R.id.btnImprimir)
        btnConsultar = v.findViewById(R.id.btnConsultar)
//        val btnCerrar: Button = v.findViewById(R.id.btnCerrar)

        txtSaldo.visibility = View.INVISIBLE
        btnPagar.visibility = View.INVISIBLE
        btnImprimir.visibility = View.INVISIBLE

        btnConsultar.setOnClickListener { consultar() }
        btnPagar     .setOnClickListener { pagar() }
        btnImprimir  .setOnClickListener { imprimirCarnet() }
//        btnCerrar    .setOnClickListener { parentFragmentManager.popBackStack() }
    }

    /** Consulta saldo y decide qué botones mostrar */
    private fun consultar() {
        val dni = etDni.text.toString().trim()
        if (dni.isEmpty()) { toast("Ingrese un DNI"); return }

        val hoy = Calendar.getInstance()
        val y = hoy[Calendar.YEAR]; val m = hoy[Calendar.MONTH] + 1

        val ctx = requireContext()
        if (!CuotaMensualDao.esSocio(ctx, dni)) {
            txtSaldo.text = "El DNI no corresponde a un SOCIO"
            mostrar(vTxt = true)
            return
        }

        val debePagar = !CuotaMensualDao.yaPagoCuota(ctx, dni, y, m)

        txtSaldo.text = if (debePagar) "Saldo a abonar:\n$25.000"
        else "El socio ya abonó este mes."
        mostrar(
            vTxt = true,
            vPagar = debePagar,
            vImp = !debePagar        // si ya pagó, habilitar imprimir
        )
    }

    private fun pagar() {
        val dni = etDni.text.toString().trim()
        val ok = CuotaMensualDao.pagarCuota(requireContext(), dni, 25_000.0)
        if (ok) {
            toast("Pago registrado")
            imprimirCarnet()        // genera carnet automáticamente
            parentFragmentManager.popBackStack()
        } else toast("No se pudo registrar el pago")
    }

    /** imprime carnet sin volver a insertar cuota */
    private fun imprimirCarnet() {
        val dni = etDni.text.toString().trim()
        CuotaMensualDao.datosSocio(requireContext(), dni)?.let { socio ->
            com.example.clubdeportivo.util.CarnetPrinter.imprimir(requireContext(), socio)
        }
    }

    /** util para cambiar visibilidad de widgets */
    private fun mostrar(vTxt:Boolean=false, vPagar:Boolean=false, vImp:Boolean=false) {
        txtSaldo.visibility   = if (vTxt) View.VISIBLE else View.INVISIBLE
        btnPagar.visibility   = if (vPagar) View.VISIBLE else View.INVISIBLE
        btnImprimir.visibility= if (vImp) View.VISIBLE else View.INVISIBLE
    }

    private fun toast(msg:String)=
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
