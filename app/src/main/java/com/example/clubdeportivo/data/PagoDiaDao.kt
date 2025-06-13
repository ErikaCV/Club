package com.example.clubdeportivo.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.clubdeportivo.data.DBContract.PagoDiaEntry
import com.example.clubdeportivo.data.DBContract.ClienteEntry

object PagoDiaDao {
    fun yaPagoHoy(ctx: Context, dni: String): Boolean {
        val db = DBHelper(ctx).readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM ${PagoDiaEntry.TABLE} pd " +
                    "JOIN ${ClienteEntry.TABLE} cl ON pd.${PagoDiaEntry.COL_CLIENTE}=cl.${ClienteEntry.COL_ID} " +
                    "WHERE cl.${ClienteEntry.COL_DNI}=? AND pd.${PagoDiaEntry.COL_FECHA}=date('now')",
            arrayOf(dni)
        )
        val paid = cursor.moveToFirst()
        cursor.close()
        return paid
    }


    fun pagarDia(ctx: Context, dni: String, monto: Double): Boolean {
        val db = DBHelper(ctx).writableDatabase
        val cur = db.rawQuery(
            "SELECT ${ClienteEntry.COL_ID} FROM ${ClienteEntry.TABLE} WHERE ${ClienteEntry.COL_DNI}=?",
            arrayOf(dni)
        )
        if (!cur.moveToFirst()) { cur.close(); return false }
        val clienteId = cur.getInt(0)
        cur.close()

        val values = ContentValues().apply {
            put(PagoDiaEntry.COL_CLIENTE, clienteId)
        }
        val res = db.insertWithOnConflict(
            PagoDiaEntry.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE
        )
        return res != -1L
    }
}