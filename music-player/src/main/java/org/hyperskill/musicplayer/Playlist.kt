package org.hyperskill.musicplayer

import java.io.Serializable

data class Playlist(val songsMap: Map<Long, Song>, val name: String) : Serializable {
    fun songsList(): List<Song> = songsMap.values.toList()

    companion object {
        fun emptyPlaylist(): Playlist {
            return Playlist(mapOf(), "Empty")
        }

        fun allSongsPlaylist(): Playlist {
            return Playlist(mapOf(), "All Songs")
        }
    }
}
