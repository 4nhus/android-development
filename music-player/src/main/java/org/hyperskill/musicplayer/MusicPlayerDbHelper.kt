package org.hyperskill.musicplayer

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MusicPlayerDbHelper(context: Context) :
    SQLiteOpenHelper(context, MusicPlayerModel.DATABASE_NAME, null, 1) {
    override fun onConfigure(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = 1")
        db.execSQL("PRAGMA trusted_schema = 0")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS ${MusicPlayerModel.TABLE_NAME} (
                ${MusicPlayerModel.PLAYLIST_NAME_COLUMN} TEXT NOT NULL,
                ${MusicPlayerModel.SONG_ID_COLUMN} INTEGER NOT NULL,
                PRIMARY KEY (${MusicPlayerModel.PLAYLIST_NAME_COLUMN}, ${MusicPlayerModel.SONG_ID_COLUMN})
            )
        """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        throw UnsupportedOperationException()
    }
}