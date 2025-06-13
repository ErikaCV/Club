package com.example.clubdeportivo.data

import android.provider.BaseColumns

object DBContract {
    object UsuarioEntry : BaseColumns {
        const val TABLE   = "usuarios"
        const val COL_ID  = BaseColumns._ID
        const val COL_USER = "usuario"
        const val COL_PASS = "password"
    }

    object ClienteEntry : BaseColumns {
        const val TABLE = "clientes"
        const val COL_ID   = BaseColumns._ID
        const val COL_NOMBRE = "nombre"
        const val COL_DNI    = "dni"
        const val COL_TIPO   = "tipo"
        const val COL_EMAIL  = "email"
        const val COL_FNAC   = "fecha_nac"
        const val COL_DIR    = "direccion"
        const val COL_CEL    = "celular"
    }
    object CuotaMesEntry : BaseColumns {
        const val TABLE       = "cuotas_mensuales"
        const val COL_ID      = BaseColumns._ID
        const val COL_CLIENTE = "cliente_id"
        const val COL_YEAR    = "anio"
        const val COL_MONTH   = "mes"
        const val COL_DAY   = "dia"
        const val COL_MONTO   = "monto"
        const val COL_VENCE    = "vence"

        const val COL_NOMBRE  = "nombre"
        const val COL_DNI     = "dni"
    }

    object ActividadEntry : BaseColumns {
        const val TABLE       = "actividades"
        const val COL_ID      = BaseColumns._ID
        const val COL_NOMBRE  = "nombre"
        const val COL_PRECIO  = "precio_individual"
    }
    object PagoDiaEntry : BaseColumns {
        const val TABLE       = "pagos_dia"
        const val COL_ID      = BaseColumns._ID
        const val COL_CLIENTE = "cliente_id"
        const val COL_FECHA   = "fecha"
    }


    object InscripcionEntry : BaseColumns {
        const val TABLE         = "inscripciones"
        const val COL_ID        = BaseColumns._ID
        const val COL_CLIENTE   = "cliente_id"
        const val COL_ACTIVIDAD = "actividad_id"
        const val COL_HORARIO   = "horario"
        const val COL_FECHA     = "fecha"
    }
}

