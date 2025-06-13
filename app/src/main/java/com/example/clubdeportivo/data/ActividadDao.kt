package com.example.clubdeportivo.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

object ActividadDao {


    fun insertar(ctx: Context, nombre: String, precio: Double): Boolean {
        val db = DBHelper(ctx).writableDatabase
        val v = ContentValues().apply {
            put(DBContract.ActividadEntry.COL_NOMBRE, nombre.trim())
            put(DBContract.ActividadEntry.COL_PRECIO, precio)
        }
        val id = db.insert(DBContract.ActividadEntry.TABLE, null, v)
        db.close()
        return id != -1L
    }


    fun todas(ctx: Context): Cursor {
        val db = DBHelper(ctx).readableDatabase
        return db.rawQuery(
            """
        SELECT ${DBContract.ActividadEntry.COL_ID}         AS _id,          -- necesario si alguna vez usas SimpleCursorAdapter
               ${DBContract.ActividadEntry.COL_NOMBRE}     AS nombre,
               ${DBContract.ActividadEntry.COL_PRECIO}     AS precio_individual
        FROM   ${DBContract.ActividadEntry.TABLE}
        ORDER  BY nombre
        """.trimIndent(), null
        )
    }


    fun listar(ctx: Context): Cursor = todas(ctx)


    fun precioDe(ctx: Context, nombre: String): Double? {
        DBHelper(ctx).readableDatabase.query(
            DBContract.ActividadEntry.TABLE,
            arrayOf(DBContract.ActividadEntry.COL_PRECIO),
            "${DBContract.ActividadEntry.COL_NOMBRE}=?",
            arrayOf(nombre), null, null, null
        ).use { c ->
            return if (c.moveToFirst()) c.getDouble(0) else null
        }
    }
}

