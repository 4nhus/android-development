package org.hyperskill.musicplayer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class SongSelectorListAdapter(
    viewModel: MusicPlayerViewModel,
    songs: List<Song> = viewModel.model.addPlaylist.songsList()
) : AbstractSongListAdapter(viewModel, songs) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongSelectorViewHolder {
        return SongSelectorViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_song_selector, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]

        (holder as SongSelectorViewHolder).apply {
            checkbox.isChecked = song.isSelected
            artist.text = song.artist
            title.text = song.title
            duration.text = song.durationString()
            itemView.apply {
                setBackgroundColor(if (song.isSelected) Color.LTGRAY else Color.WHITE)
                setOnClickListener {
                    viewModel.selectSong(position)
                }
            }
        }
    }

    class SongSelectorViewHolder(view: View) : ViewHolder(view) {
        val checkbox: CheckBox = view.findViewById(R.id.songSelectorItemCheckBox)
        val artist: TextView = view.findViewById(R.id.songSelectorItemTvArtist)
        val title: TextView = view.findViewById(R.id.songSelectorItemTvTitle)
        val duration: TextView = view.findViewById(R.id.songSelectorItemTvDuration)
    }
}