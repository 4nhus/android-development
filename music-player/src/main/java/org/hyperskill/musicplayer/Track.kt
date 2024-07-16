package org.hyperskill.musicplayer

import java.io.Serializable

data class Track(var song: Song?, var position: Int) : Serializable