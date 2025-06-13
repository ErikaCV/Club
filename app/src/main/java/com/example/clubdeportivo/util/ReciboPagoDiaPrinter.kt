package com.example.clubdeportivo.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.*
import android.view.LayoutInflater
import android.view.View
import com.example.clubdeportivo.R
import android.widget.TextView
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ReciboDiaDto(
    val dni   : String,
    val nombre: String,
    val fecha : LocalDate,
    val monto : Double
)

object ReciboPagoDiaPrinter {

    fun imprimir(ctx: Context, dto: ReciboDiaDto) {

        /* 1) Render de la vista */
        val v: View = LayoutInflater.from(ctx)
            .inflate(R.layout.card_pago_dia, null, false)

        v.findViewById<TextView>(R.id.tvNombre).text =
            "Nombre: ${dto.nombre}"                               // ← NUEVO
        v.findViewById<TextView>(R.id.tvDni   ).text =
            "DNI:    ${dto.dni}"
        v.findViewById<TextView>(R.id.tvFecha ).text =
            "Fecha:  ${dto.fecha.format(DateTimeFormatter.ISO_DATE)}"
        v.findViewById<TextView>(R.id.tvMonto ).text =
            "Importe: $ ${"%,.2f".format(dto.monto)}"

        v.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        v.layout(0, 0, v.measuredWidth, v.measuredHeight)
        val bmp = Bitmap.createBitmap(
            v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888
        )
        v.draw(Canvas(bmp))

        val pdf = PdfDocument().apply {
            val page = startPage(
                PdfDocument.PageInfo.Builder(bmp.width, bmp.height, 1).create()
            )
            page.canvas.drawBitmap(bmp, 0f, 0f, null)
            finishPage(page)
        }

        val pm = ctx.getSystemService(Context.PRINT_SERVICE) as PrintManager
        pm.print(
            "Recibo_${dto.dni}_${dto.fecha}",
            object : PrintDocumentAdapter() {

                override fun onLayout(
                    oldAttrs: PrintAttributes?, newAttrs: PrintAttributes?,
                    cancel  : CancellationSignal?,
                    cb      : LayoutResultCallback, extras: Bundle?
                ) {
                    if (cancel?.isCanceled == true) {
                        cb.onLayoutCancelled(); return
                    }
                    cb.onLayoutFinished(
                        PrintDocumentInfo.Builder("ReciboPagoDia.pdf")
                            .setPageCount(1)
                            .build(),
                        true
                    )
                }

                override fun onWrite(
                    pages : Array<PageRange>, dest: ParcelFileDescriptor,
                    cancel: CancellationSignal,
                    cb    : WriteResultCallback
                ) {
                    if (cancel.isCanceled) {
                        cb.onWriteCancelled(); return
                    }
                    pdf.writeTo(FileOutputStream(dest.fileDescriptor))
                    pdf.close()
                    cb.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                }
            },
            null
        )
    }
}
