package com.example.clubdeportivo.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.print.*
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.clubdeportivo.R
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.os.CancellationSignal
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintAttributes
import android.print.PrintManager


data class SocioDto(
    val nombre: String,
    val dni: String,
    val direccion: String?,
    val celular: String?,
    val vencimiento: LocalDate
)

object CarnetPrinter {

    fun imprimir(ctx: Context, socio: SocioDto) {

        val v: View = LayoutInflater.from(ctx)
            .inflate(R.layout.card_socio, null, false)

        v.findViewById<TextView>(R.id.tvNombre).text = "Nombre: ${socio.nombre}"
        v.findViewById<TextView>(R.id.tvDni   ).text = "DNI:    ${socio.dni}"
        v.findViewById<TextView>(R.id.tvDir   ).text = "Dir.:   ${socio.direccion ?: "-"}"
        v.findViewById<TextView>(R.id.tvCel   ).text = "Cel.:   ${socio.celular   ?: "-"}"
        v.findViewById<TextView>(R.id.tvVto   ).text =
            "Vto. cuota: ${socio.vencimiento.format(DateTimeFormatter.ISO_DATE)}"

        v.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        v.layout(0, 0, v.measuredWidth, v.measuredHeight)
        val bmp = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
        v.draw(Canvas(bmp))

        val pdf = PdfDocument()
        val page = pdf.startPage(
            PdfDocument.PageInfo.Builder(bmp.width, bmp.height, 1).create()
        )
        page.canvas.drawBitmap(bmp, 0f, 0f, null)
        pdf.finishPage(page)

        val pm = ctx.getSystemService(Context.PRINT_SERVICE) as PrintManager
        pm.print(
            "Carnet_${socio.dni}",
            object : PrintDocumentAdapter() {

                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,          // 👈 NUEVO
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder("Carnet_${socio.dni}.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pageRanges: Array<PageRange>,
                    destination: ParcelFileDescriptor,
                    cancellationSignal: CancellationSignal?,          // 👈 NUEVO
                    callback: WriteResultCallback
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onWriteCancelled()
                        return
                    }
                    pdf.writeTo(FileOutputStream(destination.fileDescriptor))
                    pdf.close()
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                }
            },
            null
        )
    }
}
