package org.hyperskill.musicplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class MainAddPlaylistFragment(private val viewModel: MusicPlayerViewModel) : Fragment() {
    private lateinit var cancelButton: Button
    private lateinit var okButton: Button
    private lateinit var playlistNameEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancelButton = view.findViewById(R.id.addPlaylistBtnCancel)
        okButton = view.findViewById(R.id.addPlaylistBtnOk)
        playlistNameEditText = view.findViewById(R.id.addPlaylistEtPlaylistName)

        cancelButton.setOnClickListener {
            viewModel.setPlayMusicState()
        }

        okButton.setOnClickListener {
            val selectedSongs = viewModel.model.addPlaylist.songsList().filter { it.isSelected }
            val playlistName = (playlistNameEditText as TextView).text.toString()

            when {
                selectedSongs.isEmpty() -> {
                    Toast.makeText(
                        context,
                        "Add at least one song to your playlist",
                        Toast.LENGTH_LONG
                    ).show()
                }

                playlistName.isBlank() -> {
                    Toast.makeText(context, "Add a name to your playlist", Toast.LENGTH_LONG).show()
                }

                playlistName == "All Songs" -> {
                    Toast.makeText(
                        context,
                        "All Songs is a reserved name choose another playlist name",
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {
                    val songsMap = selectedSongs.associateBy { it.id }
                    viewModel.savePlaylist(Playlist(songsMap, playlistName))
                }
            }
        }
    }
}