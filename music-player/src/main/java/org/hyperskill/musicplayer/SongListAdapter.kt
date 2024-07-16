package org.hyperskill.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class SongListAdapter(
    viewModel: MusicPlayerViewModel,
    songs: List<Song> = viewModel.model.currentPlaylist.songsList()
) : AbstractSongListAdapter(viewModel, songs) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_song, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]

        (holder as SongViewHolder).apply {
            button.setImageResource(if (song.state == SongState.PLAYING) R.drawable.ic_pause else R.drawable.ic_play)
            artist.text = song.artist
            title.text = song.title
            duration.text = song.durationString()

            button.setOnClickListener {
                if (viewModel.getCurrentTrackPosition() != position) {
                    viewModel.setCurrentTrackToPosition(position)
                }

                viewModel.playPauseCurrentTrack()
            }

            itemView.setOnLongClickListener {
                viewModel.model.allSongsPlaylist.songsMap.apply {
                    values.forEach { it.isSelected = false }
                }.also {
                    it[song.id]!!.isSelected = true
                }

                viewModel.setAddPlaylistState()
                true
            }
        }
    }

    class SongViewHolder(view: View) : ViewHolder(view) {
        val button: ImageButton = view.findViewById(R.id.songItemImgBtnPlayPause)
        val artist: TextView = view.findViewById(R.id.songItemTvArtist)
        val title: TextView = view.findViewById(R.id.songItemTvTitle)
        val duration: TextView = view.findViewById(R.id.songItemTvDuration)
    }
}