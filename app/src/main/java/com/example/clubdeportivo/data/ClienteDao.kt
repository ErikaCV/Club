package com.example.clubdeportivo.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.example.clubdeportivo.data.DBContract.ClienteEntry
import android.database.sqlite.SQLiteConstraintException

object ClienteDao {

    fun insertar(
        ctx: Context,
        nombre: String,
        dni: String,
        esSocio: Boolean,
        email: String?,
        fnac: String?,
        direccion: String?,
        celular: String?
    ): Boolean {
        val db = DBHelper(ctx).writableDatabase
        val v = ContentValues().apply {
            put(DBContract.ClienteEntry.COL_NOMBRE , nombre)
            put(DBContract.ClienteEntry.COL_DNI    , dni)
            put(DBContract.ClienteEntry.COL_TIPO   , if (esSocio) "SOCIO" else "NO_SOCIO")
            put(DBContract.ClienteEntry.COL_EMAIL  , email)
            put(DBContract.ClienteEntry.COL_FNAC   , fnac)
            put(DBContract.ClienteEntry.COL_DIR    , direccion)
            put(DBContract.ClienteEntry.COL_CEL    , celular)
        }
        return try {
            db.insertOrThrow(DBContract.ClienteEntry.TABLE, null, v)
            true
        } catch (_: SQLiteConstraintException) {
            false            // DNI duplicado
        }
    }

    fun buscarTodos(ctx: Context): Cursor =
        DBHelper(ctx).readableDatabase.query(
            ClienteEntry.TABLE, null, null, null, null, null,
            "${ClienteEntry.COL_NOMBRE} ASC"
        )


    fun buscarPorDni(ctx: Context, dni: String): Cursor =
        DBHelper(ctx).readableDatabase.query(
            ClienteEntry.TABLE,
            null,
            "${ClienteEntry.COL_DNI} = ?",
            arrayOf(dni.trim()),
            null, null,
            "${ClienteEntry.COL_NOMBRE} ASC"
        )

    fun obtenerNombre(ctx: Context, dni: String): String? {
        val db = DBHelper(ctx).readableDatabase
        val cur = db.rawQuery(
            "SELECT ${DBContract.ClienteEntry.COL_NOMBRE} FROM ${DBContract.ClienteEntry.TABLE} WHERE ${DBContract.ClienteEntry.COL_DNI}=?",
            arrayOf(dni)
        )
        val nom = if (cur.moveToFirst()) cur.getString(0) else null
        cur.close()
        return nom
    }

    fun esNoSocio(ctx: Context, dni: String): Boolean {
        val db = DBHelper(ctx).readableDatabase
        db.query(
            DBContract.ClienteEntry.TABLE,
            arrayOf(DBContract.ClienteEntry.COL_TIPO),
            "${DBContract.ClienteEntry.COL_DNI} = ? COLLATE NOCASE",
            arrayOf(dni.trim()),
            null, null, null
        ).use { c ->
            return c.moveToFirst() &&
                    c.getString(0).trim().equals("NO_SOCIO", ignoreCase = true)
        }
    }


    fun esSocio(ctx: Context, dni: String): Boolean = !esNoSocio(ctx, dni)

    private fun esTipo(ctx: Context, dni: String, tipoEsperado: String): Boolean {
        DBHelper(ctx).readableDatabase.query(
            DBContract.ClienteEntry.TABLE,
            arrayOf(DBContract.ClienteEntry.COL_TIPO),
            "${DBContract.ClienteEntry.COL_DNI}=?",
            arrayOf(dni),
            null, null, null, "1"        // LIMIT 1
        ).use { c ->
            return c.moveToFirst() && c.getString(0) == tipoEsperado
        }
    }
    fun nombreDe(ctx: Context, dni: String): String? {
        DBHelper(ctx).readableDatabase.query(
            DBContract.ClienteEntry.TABLE,
            arrayOf(DBContract.ClienteEntry.COL_NOMBRE),
            "${DBContract.ClienteEntry.COL_DNI}=?",
            arrayOf(dni),
            null, null, null, "1"
        ).use { c ->
            return if (c.moveToFirst()) c.getString(0) else null
        }
    }
}
