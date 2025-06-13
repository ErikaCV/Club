package com.example.clubdeportivo.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.clubdeportivo.util.SocioDto

object CuotaMensualDao {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE   //  yyyy-MM-dd




    fun pagarCuota(ctx: Context, dni: String, monto: Double): Boolean {
        val db = DBHelper(ctx).writableDatabase

        // 1) Verifico que exista un SOCIO con ese DNI, y obtengo su ID:
        val clienteCursor = db.query(
            DBContract.ClienteEntry.TABLE,
            arrayOf(DBContract.ClienteEntry.COL_ID, DBContract.ClienteEntry.COL_TIPO),
            "${DBContract.ClienteEntry.COL_DNI} = ? COLLATE NOCASE",
            arrayOf(dni.trim()),
            null, null, null
        )

        val idCliente: Long = clienteCursor.use { c ->
            if (!c.moveToFirst()) {

                return false
            }

            if (c.getString(c.getColumnIndexOrThrow(DBContract.ClienteEntry.COL_TIPO)) != "SOCIO") {
                return false
            }
            c.getLong(c.getColumnIndexOrThrow(DBContract.ClienteEntry.COL_ID))
        }


        val hoy = LocalDate.now()
        val anio  = hoy.year
        val mes   = hoy.monthValue
        val dia   = hoy.dayOfMonth

        val values = ContentValues().apply {
            put(DBContract.CuotaMesEntry.COL_CLIENTE, idCliente)
            put(DBContract.CuotaMesEntry.COL_YEAR,    anio)
            put(DBContract.CuotaMesEntry.COL_MONTH,   mes)
            put(DBContract.CuotaMesEntry.COL_DAY,     dia)
            put(DBContract.CuotaMesEntry.COL_MONTO,   monto)
        }


        val result = db.insertWithOnConflict(
            DBContract.CuotaMesEntry.TABLE,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )

        db.close()
        return (result != -1L)
    }




    fun esSocio(ctx: Context, dni: String): Boolean {
        return DBHelper(ctx).readableDatabase.query(
            DBContract.ClienteEntry.TABLE,
            arrayOf(DBContract.ClienteEntry.COL_TIPO),
            "${DBContract.ClienteEntry.COL_DNI} = ? COLLATE NOCASE",
            arrayOf(dni.trim()),
            null, null, null
        ).use { c ->
            c.moveToFirst() && c.getString(0) == "SOCIO"
        }
    }


    fun yaPagoCuota(ctx: Context, dni: String, año: Int, mes: Int): Boolean {
        val db = DBHelper(ctx).readableDatabase

        val sql = """
            SELECT 1
            FROM   ${DBContract.CuotaMesEntry.TABLE} q
            JOIN   ${DBContract.ClienteEntry.TABLE} c
              ON c.${DBContract.ClienteEntry.COL_ID} = q.${DBContract.CuotaMesEntry.COL_CLIENTE}
            WHERE  c.${DBContract.ClienteEntry.COL_DNI} = ?
              AND  q.${DBContract.CuotaMesEntry.COL_YEAR}  = ?
              AND  q.${DBContract.CuotaMesEntry.COL_MONTH} = ?
            LIMIT 1
        """.trimIndent()

        return db.rawQuery(
            sql,
            arrayOf(dni.trim(), año.toString(), mes.toString())
        ).use { it.moveToFirst() }
    }




    fun vencenHoy(ctx: Context): Cursor {
        val db  = DBHelper(ctx).readableDatabase

        val sql = """
            SELECT 
              c.${DBContract.ClienteEntry.COL_NOMBRE} AS nombre,
              c.${DBContract.ClienteEntry.COL_DNI}    AS dni,
              
              -- Armamos la fecha de vencimiento (pago + 1 mes) y la formateamos a DD/MM/YYYY
              strftime(
                '%d/%m/%Y',
                q.${DBContract.CuotaMesEntry.COL_YEAR} || '-' ||
                printf('%02d', q.${DBContract.CuotaMesEntry.COL_MONTH}) || '-' ||
                printf('%02d', q.${DBContract.CuotaMesEntry.COL_DAY}),
                '+1 month'
              ) AS fechaVto
            FROM   ${DBContract.ClienteEntry.TABLE} c
            JOIN   ${DBContract.CuotaMesEntry.TABLE} q
              ON c.${DBContract.ClienteEntry.COL_ID} = q.${DBContract.CuotaMesEntry.COL_CLIENTE}
            WHERE  c.${DBContract.ClienteEntry.COL_TIPO} = 'SOCIO'
              AND  -- Aseguramos que la fecha de pago no sea futura (p.ej. un pago con fecha mal cargada)
                   date(
                     q.${DBContract.CuotaMesEntry.COL_YEAR} || '-' ||
                     printf('%02d', q.${DBContract.CuotaMesEntry.COL_MONTH}) || '-' ||
                     printf('%02d', q.${DBContract.CuotaMesEntry.COL_DAY})
                   ) <= date('now')
              AND  -- La fecha de vencimiento (pago + 1 mes) debe coincidir con hoy:
                   date(
                     q.${DBContract.CuotaMesEntry.COL_YEAR} || '-' ||
                     printf('%02d', q.${DBContract.CuotaMesEntry.COL_MONTH}) || '-' ||
                     printf('%02d', q.${DBContract.CuotaMesEntry.COL_DAY}),
                     '+1 month'
                   ) = date('now')
            ORDER BY c.${DBContract.ClienteEntry.COL_NOMBRE} ASC
        """.trimIndent()

        return db.rawQuery(sql, null)
    }




    fun datosSocio(ctx: Context, dni: String): SocioDto? {
        val db = DBHelper(ctx).readableDatabase
        val c = db.rawQuery(
            """
            SELECT 
              ${DBContract.ClienteEntry.COL_NOMBRE}, 
              ${DBContract.ClienteEntry.COL_DIR}, 
              ${DBContract.ClienteEntry.COL_CEL}
            FROM ${DBContract.ClienteEntry.TABLE} 
            WHERE ${DBContract.ClienteEntry.COL_DNI} = ?
            """.trimIndent(),
            arrayOf(dni.trim())
        )

        val socio = if (c.moveToFirst()) {
            SocioDto(
                nombre      = c.getString(0),
                dni         = dni.trim(),
                direccion   = c.getString(1),
                celular     = c.getString(2),

                vencimiento = LocalDate.now().plusMonths(1)
            )
        } else {
            null
        }
        c.close()
        return socio
    }
}