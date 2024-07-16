package org.hyperskill.musicplayer

import java.io.Serializable

fun convertMinutesSecondsToDuration(minutes: Int, seconds: Int): String =
    "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"

fun Int.durationMillisecondsString(): String =
    convertMinutesSecondsToDuration(this / 60000, (this / 1000) % 60)

fun Int.durationSecondsString(): String = convertMinutesSecondsToDuration(this / 60, this % 60)

data class Song(
    val id: Long,
    val artist: String,
    val title: String,
    val durationMilliseconds: Int,
    var state: SongState = SongState.STOPPED,
    var isSelected: Boolean = false
) : Serializable {
    fun durationString() = durationMilliseconds.durationMillisecondsString()
}