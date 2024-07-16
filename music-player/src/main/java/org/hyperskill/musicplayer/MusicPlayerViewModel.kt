package org.hyperskill.musicplayer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentUris
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
class MusicPlayerViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val view: MainActivity
) : ViewModel() {
    val model = MusicPlayerModel(
        savedStateHandle["state"] ?: MusicPlayerState.PLAY_MUSIC,
        savedStateHandle["currentPlaylist"] ?: Playlist.emptyPlaylist(),
        savedStateHandle["addPlaylist"] ?: Playlist.emptyPlaylist(),
        savedStateHandle["allSongsPlaylist"] ?: Playlist.allSongsPlaylist(),
        MutableStateFlow(
            Track(
                savedStateHandle["currentTrackSong"],
                savedStateHandle["currentTrackPosition"] ?: -1
            )
        ),
        this,
        MusicPlayerDbHelper(view).writableDatabase
    )

    var mediaPlayer: MediaPlayer = MediaPlayer().apply {
        setOnCompletionListener {
            stopCurrentTrack()
        }
    }

    val currentTrack: StateFlow<Track> get() = model.currentTrack

    fun init() {
        setPlayMusicState()
        updateMediaPlayer()
    }

    private fun updateMediaPlayer() {
        currentTrack.value.song?.id?.let { id ->
            val songUri =
                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
            mediaPlayer.reset()
            mediaPlayer.setDataSource(view, songUri)
            mediaPlayer.prepare()

            if (currentTrack.value.song!!.state == SongState.PLAYING) mediaPlayer.start()
        }
    }

    private fun saveToStateHandle() {
        CoroutineScope(Dispatchers.IO).launch {
            savedStateHandle["state"] = model.state
            savedStateHandle["currentPlaylist"] = model.currentPlaylist
            savedStateHandle["addPlaylist"] = model.addPlaylist
            savedStateHandle["allSongsPlaylist"] = model.allSongsPlaylist
            savedStateHandle["currentTrackSong"] = currentTrack.value.song
            savedStateHandle["currentTrackPosition"] = currentTrack.value.position
        }
    }

    fun generateSongMapFromSearch(): Map<Long, Song> {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION
        )

        val query = view.contentResolver.query(uri, projection, null, null, null, null)

        return buildMap {
            query?.use { cursor ->
                // Get column indices
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ARTIST)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DURATION)

                while (cursor.moveToNext()) {
                    // Get values from column indices for a song
                    val id = cursor.getLong(idColumn)
                    val artist = cursor.getString(artistColumn)
                    val title = cursor.getString(titleColumn)
                    val duration = cursor.getInt(durationColumn)

                    // Add song to map
                    put(id, Song(id, artist, title, duration))
                }
            }
        }
    }

    fun setPlayMusicState() {
        model.state = MusicPlayerState.PLAY_MUSIC

        view.apply {
            setAdapter(SongListAdapter(this@MusicPlayerViewModel))
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.mainFragmentContainer,
                    MainPlayerControllerFragment(this@MusicPlayerViewModel)
                )
                .commit()
        }

        savedStateHandle["state"] = model.state
    }

    fun setAddPlaylistState() {
        model.apply {
            addPlaylist = model.allSongsPlaylist
            state = MusicPlayerState.ADD_PLAYLIST
        }

        view.apply {
            setAdapter(SongSelectorListAdapter(this@MusicPlayerViewModel))
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.mainFragmentContainer,
                    MainAddPlaylistFragment(this@MusicPlayerViewModel)
                )
                .commit()
        }

        savedStateHandle["state"] = model.state
    }

    fun search() {
        val searchPlaylist =
            Playlist(generateSongMapFromSearch(), "All Songs").also { searchPlaylist ->
                // If searched list contains current track maintain its state
                currentTrack.value.apply {
                    song = searchPlaylist.songsMap[song?.id]
                        ?.apply {
                            state = song!!.state
                            position = searchPlaylist.songsMap.keys.indexOf(id)
                        }
                        ?: searchPlaylist.songsList().firstOrNull()
                            .also { firstSong ->// Else just take the first song
                                position = if (firstSong == null) -1 else 0

                                // Trigger emit from StateFlow
                                model.currentTrack.value =
                                    currentTrack.value.copy(song = firstSong, position = position)

                                updateMediaPlayer()
                            }
                }
            }

        when (model.state) {
            MusicPlayerState.PLAY_MUSIC -> {
                model.apply {
                    allSongsPlaylist = searchPlaylist
                    currentPlaylist = searchPlaylist
                }

                view.setAdapter(SongListAdapter(this))
            }

            MusicPlayerState.ADD_PLAYLIST -> {
                model.apply {
                    allSongsPlaylist = searchPlaylist
                    addPlaylist = searchPlaylist
                }
                if (model.currentPlaylist.name == "All Songs") {
                    model.currentPlaylist = searchPlaylist
                }
                view.setAdapter(SongSelectorListAdapter(this))
            }
        }

        saveToStateHandle()
    }

    fun playPauseCurrentTrack() {
        currentTrack.value.song?.apply {
            // Pause current track if playing, otherwise play it
            state = when (state) {
                SongState.PLAYING -> {
                    mediaPlayer.pause()
                    SongState.PAUSED
                }

                SongState.PAUSED -> {
                    mediaPlayer.start()
                    SongState.PLAYING
                }

                SongState.STOPPED -> {
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                    SongState.PLAYING
                }
            }
        }?.also {
            // Notify adapter of state change
            view.notifyAdapterItemChangedNoAnimation(currentTrack.value.position)
        }

        saveToStateHandle()
    }

    fun stopCurrentTrack() {
        currentTrack.value.song?.apply {
            // Stop current track
            state = when (state) {
                SongState.PLAYING -> {
                    mediaPlayer.pause()
                    mediaPlayer.seekTo(0)
                    mediaPlayer.stop()
                    SongState.STOPPED
                }

                SongState.PAUSED -> {
                    mediaPlayer.seekTo(0)
                    mediaPlayer.stop()
                    SongState.STOPPED
                }

                SongState.STOPPED -> SongState.STOPPED
            }
        }?.also {
            // Notify adapter of state change
            view.notifyAdapterItemChangedNoAnimation(currentTrack.value.position)
        }

        saveToStateHandle()
    }

    fun getCurrentTrackPosition() = currentTrack.value.position

    fun setCurrentTrackToPosition(position: Int) {
        stopCurrentTrack()

        // Trigger emit from StateFlow
        model.currentTrack.value = currentTrack.value.copy(
            song = model.currentPlaylist.songsList()[position],
            position = position
        )

        updateMediaPlayer()
        saveToStateHandle()
    }


    fun selectSong(position: Int) {
        model.addPlaylist.songsList()[position].isSelected =
            !model.addPlaylist.songsList()[position].isSelected
        view.notifyAdapterItemChangedNoAnimation(position)
        saveToStateHandle()
    }

    fun addPlaylistFromMenu() {
        if (model.state == MusicPlayerState.PLAY_MUSIC) {
            if (model.allSongsPlaylist.songsMap.isEmpty()) {
                Toast.makeText(
                    view,
                    "no songs loaded, click search to load songs",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                model.allSongsPlaylist.songsMap.values.forEach { it.isSelected = false }
                setAddPlaylistState()
            }

            saveToStateHandle()
        }
    }

    fun loadPlaylistFromMenu() {
        val playlists = model.playlists.toMutableList().apply {
            add(0, model.allSongsPlaylist)
        }

        AlertDialog.Builder(view)
            .setTitle("choose playlist to load")
            .setItems(playlists.map { it.name }.toTypedArray()) { _, i ->
                // Populate All Songs playlist if empty
                if (model.allSongsPlaylist.songsMap.isEmpty()) {
                    model.allSongsPlaylist = Playlist(generateSongMapFromSearch(), "All Songs")
                }

                // Then load as usual
                if (model.state == MusicPlayerState.PLAY_MUSIC) {
                    var shouldUpdateMediaPlayer = false
                    model.currentPlaylist = playlists[i].apply {
                        // If loaded list contains current track maintain its state
                        currentTrack.value.apply {
                            song = songsMap[song?.id]
                                ?.apply {
                                    state = song!!.state
                                    position = songsMap.keys.indexOf(id)
                                }
                                ?: songsList().firstOrNull().also {// Else just take the first song
                                    position = if (it == null) -1 else 0
                                    shouldUpdateMediaPlayer = true
                                }
                        }
                    }

                    if (shouldUpdateMediaPlayer) updateMediaPlayer()

                    view.setAdapter(SongListAdapter(this))
                } else {
                    val previousPlaylistSongs = model.addPlaylist.songsMap

                    model.addPlaylist = playlists[i].apply {
                        for ((id, song) in songsMap) {
                            // If previous list contains new songs maintain their isSelected
                            song.isSelected = previousPlaylistSongs[id]?.isSelected
                                ?: false
                        }
                    }

                    view.setAdapter(SongSelectorListAdapter(this))
                }

                saveToStateHandle()
            }
            .setNegativeButton("cancel") { _, _ -> }
            .create()
            .show()
    }

    fun deletePlaylistFromMenu() {
        val playlists = model.playlists.map { it.name }.toTypedArray()

        AlertDialog.Builder(view)
            .setTitle("choose playlist to delete")
            .setItems(playlists) { _, i ->
                // Delete the playlist
                model.deletePlaylist(i)

                // Display any updates
                view.setAdapter(
                    if (model.state == MusicPlayerState.PLAY_MUSIC) SongListAdapter(this) else SongSelectorListAdapter(
                        this
                    )
                )

                saveToStateHandle()
            }
            .setNegativeButton("cancel") { _, _ -> }
            .create()
            .show()
    }

    fun updateMediaPlayerCurrentPosition(newPosition: Int) {
        mediaPlayer.apply {
            if (currentTrack.value.song?.state == SongState.STOPPED) {
                prepare()
            }

            seekTo(newPosition)
        }
    }

    fun savePlaylist(playlist: Playlist) {
        model.savePlaylist(playlist)
        setPlayMusicState()
    }

    // Define ViewModel factory in a companion object
    companion object {
        fun provideFactory(
            view: MainActivity,
            defaultArgs: Bundle? = null
        ): AbstractSavedStateViewModelFactory =
            object : AbstractSavedStateViewModelFactory(view, defaultArgs) {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return MusicPlayerViewModel(handle, view) as T
                }
            }
    }
}