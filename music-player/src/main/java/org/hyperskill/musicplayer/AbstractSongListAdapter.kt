package org.hyperskill.musicplayer

import androidx.recyclerview.widget.RecyclerView

abstract class AbstractSongListAdapter(
    protected val viewModel: MusicPlayerViewModel,
    val songs: List<Song>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount() = songs.size
}