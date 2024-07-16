package org.hyperskill.musicplayer

import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.Closeable
import java.io.Serializable

data class MusicPlayerModel(
    var state: MusicPlayerState,
    var currentPlaylist: Playlist,
    var addPlaylist: Playlist,
    var allSongsPlaylist: Playlist,
    val currentTrack: MutableStateFlow<Track>,
    private val viewModel: MusicPlayerViewModel,
    private val db: SQLiteDatabase,
) : Serializable, Closeable {
    val playlists: MutableList<Playlist> = buildList {
        val playlistMap = playlistMapFromDatabase()
        val songMap = viewModel.generateSongMapFromSearch()

        for ((playlistName, songIds) in playlistMap) {
            val playlist = Playlist(buildMap {
                songIds.forEach { id ->
                    songMap[id]?.let { song -> put(id, song.copy()) }
                }
            }, playlistName)

            add(playlist)
        }
    }.toMutableList()

    private val insertStatement = db.compileStatement(
        "INSERT INTO $TABLE_NAME ($PLAYLIST_NAME_COLUMN, $SONG_ID_COLUMN) VALUES (?, ?)"
    )

    private val deleteStatement = db.compileStatement(
        "DELETE FROM $TABLE_NAME WHERE $PLAYLIST_NAME_COLUMN = ?"
    )

    fun savePlaylist(playlist: Playlist) {
        // Remove existing songs if playlist already exists
        if (deletePlaylistFromDatabase(playlist.name) > 0) {
            val iterator = playlists.iterator()
            while (iterator.hasNext()) {
                if (iterator.next().name == playlist.name) iterator.remove()
            }
        }

        playlists.add(playlist)
        // Add songs to database
        for (id in playlist.songsMap.keys) {
            insertPlaylistSongIntoDatabase(playlist.name, id)
        }
    }

    fun deletePlaylist(index: Int) {
        // Update currentPlaylist if it is being deleted
        if (playlists[index] == currentPlaylist) {
            currentPlaylist = allSongsPlaylist
        }

        // Update addPlaylist if it is being deleted
        if (playlists[index] == addPlaylist) {
            addPlaylist = allSongsPlaylist
        }

        deletePlaylistFromDatabase(playlists[index].name)
        playlists.removeAt(index)
    }


    private fun insertPlaylistSongIntoDatabase(playlistName: String, songId: Long) {
        insertStatement.bindString(1, playlistName)
        insertStatement.bindLong(2, songId)
        insertStatement.executeInsert()
    }

    private fun deletePlaylistFromDatabase(name: String): Int {
        deleteStatement.bindString(1, name)
        return deleteStatement.executeUpdateDelete()
    }

    private fun playlistMapFromDatabase(): Map<String, MutableList<Long>> {
        val cursor = db.query(
            TABLE_NAME, TABLE_COLUMNS, null, null,
            null, null, null
        )

        val map = mutableMapOf<String, MutableList<Long>>()
        while (cursor.moveToNext()) {
            val playlistName = cursor.getString(cursor.getColumnIndexOrThrow(PLAYLIST_NAME_COLUMN))
            val songId = cursor.getLong(cursor.getColumnIndexOrThrow(SONG_ID_COLUMN))
            if (playlistName in map) {
                map[playlistName]!!.add(songId)
            } else {
                map[playlistName] = mutableListOf(songId)
            }
        }

        cursor.close()
        return map
    }

    override fun close() {
        insertStatement.close()
        deleteStatement.close()
    }

    companion object {
        const val DATABASE_NAME = "musicPlayerDatabase.db"
        const val TABLE_NAME = "playlist"
        const val PLAYLIST_NAME_COLUMN = "playlistName"
        const val SONG_ID_COLUMN = "songId"
        val TABLE_COLUMNS = arrayOf(PLAYLIST_NAME_COLUMN, SONG_ID_COLUMN)
    }
}