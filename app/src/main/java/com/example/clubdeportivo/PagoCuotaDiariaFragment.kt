package com.example.clubdeportivo
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.clubdeportivo.R
import com.example.clubdeportivo.data.ClienteDao
import com.example.clubdeportivo.data.PagoDiaDao
import java.time.LocalDate
import com.example.clubdeportivo.util.ReciboDiaDto
import com.example.clubdeportivo.util.ReciboPagoDiaPrinter


class PagoCuotaDiariaFragment : Fragment() {

    private lateinit var etDni        : EditText
    private lateinit var txtSaldo     : TextView
    private lateinit var btnPagar     : Button
    private lateinit var btnReimp     : Button


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_pago_cuota_diaria, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        etDni        = view.findViewById(R.id.editTextDni)
        txtSaldo     = view.findViewById(R.id.textSaldo)
        btnPagar     = view.findViewById(R.id.btnPagar)
        btnReimp     = view.findViewById(R.id.btnReimprimir)
        val btnCons  = view.findViewById<Button>(R.id.btnConsultar)



        txtSaldo.visibility = View.INVISIBLE
        btnPagar.visibility = View.INVISIBLE
        btnReimp.visibility = View.GONE


        btnCons   .setOnClickListener { consultar() }
        btnPagar  .setOnClickListener { pagar() }
        btnReimp  .setOnClickListener { reimprimir() }
    }


    private fun consultar() {
        val dni = etDni.text.toString().trim()
        if (dni.isEmpty()) { toast("Ingrese DNI"); return }

        // solo NO_SOCIO puede pagar día
        if (!ClienteDao.esNoSocio(requireContext(), dni)) {
            txtSaldo.text       = "El DNI no corresponde a un NO_SOCIO"
            txtSaldo.visibility = View.VISIBLE
            btnPagar.visibility = View.INVISIBLE
            btnReimp.visibility = View.GONE
            return
        }

        val pagoHoy = PagoDiaDao.yaPagoHoy(requireContext(), dni)
        if (pagoHoy) {
            txtSaldo.text       = "Ya pagó hoy – saldo $0"
            btnPagar.visibility = View.INVISIBLE
            btnReimp.visibility = View.VISIBLE
        } else {
            txtSaldo.text       = "Saldo a abonar:\n$3.000"
            btnPagar.visibility = View.VISIBLE
            btnReimp.visibility = View.GONE
        }
        txtSaldo.visibility = View.VISIBLE
    }


    private fun pagar() {
        val dni   = etDni.text.toString().trim()
        val monto = 3_000.0

        if (PagoDiaDao.pagarDia(requireContext(), dni, monto)) {

            val nombreCli = ClienteDao.nombreDe(requireContext(), dni) ?: "(sin nombre)"

            val dto = ReciboDiaDto(
                dni    = dni,
                nombre = nombreCli,
                fecha  = LocalDate.now(),
                monto  = monto
            )
            ReciboPagoDiaPrinter.imprimir(requireContext(), dto)

            toast("Pago registrado")


            consultar()
        } else {
            toast("Error registrando pago")
        }
    }

    // ─────────────  RE-IMPRIMIR sin tocar la BD  ─────────────────
    private fun reimprimir() {
        val dni = etDni.text.toString().trim()
        imprimirRecibo(dni, 3_000.0)
    }

    // ─────────────────  Helper común  ────────────────────────────
    private fun imprimirRecibo(dni: String, monto: Double) {
        val nombreCli = ClienteDao.nombreDe(requireContext(), dni) ?: "(sin nombre)"

        val dto = ReciboDiaDto(
            dni    = dni,
            nombre = nombreCli,
            fecha  = LocalDate.now(),
            monto  = monto
        )
        ReciboPagoDiaPrinter.imprimir(requireContext(), dto)
    }

    private fun toast(m: String) =
        Toast.makeText(requireContext(), m, Toast.LENGTH_SHORT).show()
}
