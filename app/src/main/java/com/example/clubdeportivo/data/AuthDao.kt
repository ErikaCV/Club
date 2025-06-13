package com.example.clubdeportivo.data

import android.content.Context
import com.example.clubdeportivo.data.DBContract.UsuarioEntry

object AuthDao {


    fun login(ctx: Context, user: String, pass: String): Boolean {
        val db = DBHelper(ctx).readableDatabase
        val cursor = db.query(
            UsuarioEntry.TABLE,
            arrayOf(UsuarioEntry.COL_ID),
            "${UsuarioEntry.COL_USER}=? AND ${UsuarioEntry.COL_PASS}=?",
            arrayOf(user, pass),
            null, null, null
        )
        val ok = cursor.count > 0
        cursor.close()
        db.close()
        return ok
    }
}
