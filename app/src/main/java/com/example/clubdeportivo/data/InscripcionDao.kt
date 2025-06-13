package com.example.clubdeportivo.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.clubdeportivo.data.DBContract.InscripcionEntry
import com.example.clubdeportivo.data.DBContract.ClienteEntry
import com.example.clubdeportivo.data.DBContract.ActividadEntry
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.util.Log

object InscripcionDao {
    /** true si el cliente‑dni ya está inscripto a la actividad + horario */
    fun yaInscripto(ctx: Context, dni: String, actNombre: String, horario: String): Boolean {
        val db = DBHelper(ctx).readableDatabase
        val c = db.rawQuery(
            """
            SELECT 1 FROM ${InscripcionEntry.TABLE} i
             JOIN ${ClienteEntry.TABLE} cl ON i.${InscripcionEntry.COL_CLIENTE}=cl.${ClienteEntry.COL_ID}
             JOIN ${ActividadEntry.TABLE} a ON i.${InscripcionEntry.COL_ACTIVIDAD}=a.${ActividadEntry.COL_ID}
            WHERE cl.${ClienteEntry.COL_DNI}=? AND a.${ActividadEntry.COL_NOMBRE}=? AND i.${InscripcionEntry.COL_HORARIO}=?
            """.trimIndent(), arrayOf(dni, actNombre, horario)
        )
        val ok = c.moveToFirst(); c.close(); return ok
    }


    fun inscribir(
        ctx: Context,
        dni: String,
        actNombre: String,
        horario: String
    ): Boolean {

        val db = DBHelper(ctx).writableDatabase
        db.setForeignKeyConstraintsEnabled(true)


        val curC = db.rawQuery(
            "SELECT ${ClienteEntry.COL_ID}, ${ClienteEntry.COL_TIPO} " +
                    "FROM   ${ClienteEntry.TABLE} " +
                    "WHERE  ${ClienteEntry.COL_DNI} = ?",
            arrayOf(dni)
        )
        if (!curC.moveToFirst()) {
            curC.close(); return false
        }
        val clienteId = curC.getInt(
            curC.getColumnIndexOrThrow(ClienteEntry.COL_ID)
        )
        val tipo = curC.getString(
            curC.getColumnIndexOrThrow(ClienteEntry.COL_TIPO)
        )
        curC.close()
        if (tipo != "NO_SOCIO") return false


        val curA = db.rawQuery(
            "SELECT ${ActividadEntry.COL_ID} " +
                    "FROM   ${ActividadEntry.TABLE} " +
                    "WHERE  ${ActividadEntry.COL_NOMBRE} = ?",
            arrayOf(actNombre)
        )
        if (!curA.moveToFirst()) {            // actividad inexistente
            curA.close(); return false
        }
        val actId = curA.getInt(
            curA.getColumnIndexOrThrow(ActividadEntry.COL_ID)
        )
        curA.close()

        /* ── 3. Insertar inscripción ─────────────────────────────── */
        val values = ContentValues().apply {
            put(InscripcionEntry.COL_CLIENTE , clienteId)
            put(InscripcionEntry.COL_ACTIVIDAD, actId)
            put(InscripcionEntry.COL_HORARIO , horario)
            // COL_FECHA tiene valor por defecto (date('now'))
        }

        return try {
            db.insertOrThrow(InscripcionEntry.TABLE, null, values)
            true
        } catch (e: SQLiteConstraintException) {   // violación de UNIQUE, FK, etc.
            false
        }
    }

    fun obtenerTodos(ctx: Context): Cursor {
        val db = DBHelper(ctx).readableDatabase
        return db.rawQuery(
            """
            SELECT a.${ActividadEntry.COL_NOMBRE} AS actividad,
                   cl.${ClienteEntry.COL_NOMBRE}  AS nombre,
                   cl.${ClienteEntry.COL_DNI}     AS dni,
                   i.${InscripcionEntry.COL_HORARIO} AS horario,
                   i.${InscripcionEntry.COL_FECHA}   AS fecha,
                   i.${InscripcionEntry.COL_HORARIO} AS horario
            FROM ${InscripcionEntry.TABLE} i
             JOIN ${ClienteEntry.TABLE} cl ON i.${InscripcionEntry.COL_CLIENTE}=cl.${ClienteEntry.COL_ID}
             JOIN ${ActividadEntry.TABLE} a ON i.${InscripcionEntry.COL_ACTIVIDAD}=a.${ActividadEntry.COL_ID}
            ORDER BY a.${ActividadEntry.COL_NOMBRE}, cl.${ClienteEntry.COL_NOMBRE}
            """.trimIndent(), null
        )
    }
}
