package com.example.clubdeportivo.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.clubdeportivo.data.DBContract.UsuarioEntry
import com.example.clubdeportivo.data.DBContract.CuotaMesEntry
import com.example.clubdeportivo.data.DBContract.ClienteEntry
import com.example.clubdeportivo.data.DBContract.ActividadEntry
import com.example.clubdeportivo.data.DBContract.PagoDiaEntry
import com.example.clubdeportivo.data.DBContract.InscripcionEntry
private const val DB_NAME = "club.db"
private const val DB_VERSION = 10

class DBHelper(ctx: Context) : SQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }


    override fun onCreate(db: SQLiteDatabase) {


        db.execSQL("""
            CREATE TABLE ${UsuarioEntry.TABLE}(
              ${UsuarioEntry.COL_ID}   INTEGER PRIMARY KEY AUTOINCREMENT,
              ${UsuarioEntry.COL_USER} TEXT UNIQUE NOT NULL,
              ${UsuarioEntry.COL_PASS} TEXT NOT NULL
            )
        """)
        val admin = ContentValues().apply {
            put(UsuarioEntry.COL_USER, "admin")
            put(UsuarioEntry.COL_PASS, "admin123")
        }
        db.insert(UsuarioEntry.TABLE, null, admin)

        db.execSQL("""
            CREATE TABLE ${ClienteEntry.TABLE}(
              ${ClienteEntry.COL_ID}    INTEGER PRIMARY KEY AUTOINCREMENT,
              ${ClienteEntry.COL_NOMBRE} TEXT NOT NULL,
              ${ClienteEntry.COL_DNI}    TEXT UNIQUE NOT NULL,
              ${ClienteEntry.COL_TIPO}   TEXT CHECK(${ClienteEntry.COL_TIPO} IN ('SOCIO','NO_SOCIO')),
              ${ClienteEntry.COL_EMAIL}  TEXT,
              ${ClienteEntry.COL_FNAC}   TEXT,
              ${ClienteEntry.COL_DIR}    TEXT,
              ${ClienteEntry.COL_CEL}    TEXT,
              fecha_alta TEXT NOT NULL DEFAULT (date('now'))
            )
        """)

        db.execSQL("""
            CREATE TABLE ${CuotaMesEntry.TABLE}(
              ${CuotaMesEntry.COL_ID}      INTEGER PRIMARY KEY AUTOINCREMENT,
              ${CuotaMesEntry.COL_CLIENTE} INTEGER NOT NULL,
              ${CuotaMesEntry.COL_YEAR}    INTEGER NOT NULL,
              ${CuotaMesEntry.COL_MONTH}   INTEGER NOT NULL,
              ${CuotaMesEntry.COL_DAY}     INTEGER NOT NULL DEFAULT (strftime('%d','now')), 
              ${CuotaMesEntry.COL_MONTO}   REAL    NOT NULL,
               UNIQUE(${CuotaMesEntry.COL_CLIENTE},
             ${CuotaMesEntry.COL_YEAR},
             ${CuotaMesEntry.COL_MONTH},
             ${CuotaMesEntry.COL_DAY}),
              FOREIGN KEY(${CuotaMesEntry.COL_CLIENTE})
                  REFERENCES ${ClienteEntry.TABLE}(${ClienteEntry.COL_ID}) ON DELETE CASCADE
            )
        """)

        crearTablaActividades   (db)
        insertarActividadesSemilla(db)
        crearTablaPagosDia      (db)
        crearTablaInscripciones (db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        if (oldV < 5) {
            crearTablaActividades(db)
            insertarActividadesSemilla(db)
        }
        if (oldV < 6) {
            crearTablaPagosDia(db)
        }
        if (oldV < 7) {
            crearTablaInscripciones(db)
        }
        if (oldV < 8) {
            db.execSQL("""
            ALTER TABLE ${InscripcionEntry.TABLE} 
            ADD COLUMN ${InscripcionEntry.COL_HORARIO} TEXT NOT NULL DEFAULT '12–14'
        """.trimIndent())
            db.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS idx_insc 
            ON ${InscripcionEntry.TABLE}(
              ${InscripcionEntry.COL_CLIENTE},
              ${InscripcionEntry.COL_ACTIVIDAD},
              ${InscripcionEntry.COL_HORARIO}
            )
        """.trimIndent())
        }
        if (oldV < 9) {
            db.execSQL("ALTER TABLE ${ClienteEntry.TABLE} ADD COLUMN ${ClienteEntry.COL_FNAC} TEXT")
            db.execSQL("ALTER TABLE ${ClienteEntry.TABLE} ADD COLUMN ${ClienteEntry.COL_DIR}  TEXT")
            db.execSQL("ALTER TABLE ${ClienteEntry.TABLE} ADD COLUMN ${ClienteEntry.COL_CEL}  TEXT")
        }

        // ─────────── NUEVO BLOQUE PARA v10 ───────────
        if (oldV < 10) {
            db.execSQL("""
            CREATE TABLE cuotas_mens_temp (
              ${CuotaMesEntry.COL_ID}       INTEGER PRIMARY KEY AUTOINCREMENT,
              ${CuotaMesEntry.COL_CLIENTE}  INTEGER NOT NULL,
              ${CuotaMesEntry.COL_YEAR}     INTEGER NOT NULL,
              ${CuotaMesEntry.COL_MONTH}    INTEGER NOT NULL,
              ${CuotaMesEntry.COL_DAY}      INTEGER NOT NULL DEFAULT(strftime('%d','now')),
              ${CuotaMesEntry.COL_MONTO}    REAL    NOT NULL,
              UNIQUE(${CuotaMesEntry.COL_CLIENTE},
                     ${CuotaMesEntry.COL_YEAR},
                     ${CuotaMesEntry.COL_MONTH},
                     ${CuotaMesEntry.COL_DAY}),
              FOREIGN KEY(${CuotaMesEntry.COL_CLIENTE})
                  REFERENCES ${ClienteEntry.TABLE}(${ClienteEntry.COL_ID}) ON DELETE CASCADE
            )
        """.trimIndent())


            db.execSQL("""
            INSERT INTO cuotas_mens_temp
              (${CuotaMesEntry.COL_ID},
               ${CuotaMesEntry.COL_CLIENTE},
               ${CuotaMesEntry.COL_YEAR},
               ${CuotaMesEntry.COL_MONTH},
               ${CuotaMesEntry.COL_DAY},
               ${CuotaMesEntry.COL_MONTO})
            SELECT
              ${CuotaMesEntry.COL_ID},
              ${CuotaMesEntry.COL_CLIENTE},
              ${CuotaMesEntry.COL_YEAR},
              ${CuotaMesEntry.COL_MONTH},
              strftime('%d','now') AS dia,
              ${CuotaMesEntry.COL_MONTO}
            FROM ${CuotaMesEntry.TABLE}
        """.trimIndent())


            db.execSQL("DROP TABLE ${CuotaMesEntry.TABLE}")
            db.execSQL("ALTER TABLE cuotas_mens_temp RENAME TO ${CuotaMesEntry.TABLE}")
        }

    }


    private fun crearTablaActividades(db: SQLiteDatabase) =
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS ${ActividadEntry.TABLE}(
              ${ActividadEntry.COL_ID}     INTEGER PRIMARY KEY AUTOINCREMENT,
              ${ActividadEntry.COL_NOMBRE} TEXT UNIQUE NOT NULL,
              ${ActividadEntry.COL_PRECIO} REAL  NOT NULL
            )
        """)

    private fun insertarActividadesSemilla(db: SQLiteDatabase) {
        val iniciales = listOf(
            "Vóley"      to 3000.0,
            "Tenis"      to 3500.0,
            "Fútbol"     to 3000.0,
            "Básquet"    to 3200.0,
            "Natación"   to 3800.0
        )
        val stmt = db.compileStatement(
            "INSERT OR IGNORE INTO ${ActividadEntry.TABLE} " +
                    "(${ActividadEntry.COL_NOMBRE},${ActividadEntry.COL_PRECIO}) VALUES (?,?)"
        )
        iniciales.forEach { (nom, pre) ->
            stmt.bindString(1, nom)
            stmt.bindDouble(2, pre)
            stmt.executeInsert()
        }
        stmt.close()
    }

    private fun crearTablaPagosDia(db: SQLiteDatabase) =
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS ${PagoDiaEntry.TABLE}(
              ${PagoDiaEntry.COL_ID}      INTEGER PRIMARY KEY AUTOINCREMENT,
              ${PagoDiaEntry.COL_CLIENTE} INTEGER NOT NULL,
              ${PagoDiaEntry.COL_FECHA}   TEXT NOT NULL DEFAULT (date('now')),
              UNIQUE(${PagoDiaEntry.COL_CLIENTE},${PagoDiaEntry.COL_FECHA}),
              FOREIGN KEY(${PagoDiaEntry.COL_CLIENTE})
                  REFERENCES ${ClienteEntry.TABLE}(${ClienteEntry.COL_ID}) ON DELETE CASCADE
            )
        """)

    private fun crearTablaInscripciones(db: SQLiteDatabase) =
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS ${InscripcionEntry.TABLE}(
              ${InscripcionEntry.COL_ID}        INTEGER PRIMARY KEY AUTOINCREMENT,
              ${InscripcionEntry.COL_CLIENTE}   INTEGER NOT NULL,
              ${InscripcionEntry.COL_ACTIVIDAD} INTEGER NOT NULL,
              ${InscripcionEntry.COL_HORARIO}   TEXT NOT NULL,
              ${InscripcionEntry.COL_FECHA}     TEXT NOT NULL DEFAULT (date('now')),
              UNIQUE(${InscripcionEntry.COL_CLIENTE},
                     ${InscripcionEntry.COL_ACTIVIDAD},
                     ${InscripcionEntry.COL_HORARIO}),
              FOREIGN KEY(${InscripcionEntry.COL_CLIENTE})
                   REFERENCES ${ClienteEntry.TABLE}(${ClienteEntry.COL_ID}) ON DELETE CASCADE,
              FOREIGN KEY(${InscripcionEntry.COL_ACTIVIDAD})
                   REFERENCES ${ActividadEntry.TABLE}(${ActividadEntry.COL_ID}) ON DELETE CASCADE
            )
        """)
}