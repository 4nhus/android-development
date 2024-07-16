package org.hyperskill.musicplayer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

class MainPlayerControllerFragment(private val viewModel: MusicPlayerViewModel) : Fragment() {
    private val handler = Handler(Looper.getMainLooper())
    private val syncControllerSeekBarProgress: Runnable = object : Runnable {
        override fun run() {
            controllerSeekBar.progress = viewModel.mediaPlayer.currentPosition / 1000
            handler.postDelayed(this, 50)
        }
    }

    private lateinit var playPauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var controllerSeekBar: SeekBar
    private lateinit var controllerCurrentTimeTextView: TextView
    private lateinit var controllerTotalTimeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentTrack.collect { currentTrack ->
                    controllerSeekBar.max = (currentTrack.song?.durationMilliseconds
                        ?: 0) / 1000
                    controllerTotalTimeTextView.text = currentTrack.song?.durationString()
                        ?: "00:00"
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.player_controller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playPauseButton = view.findViewById<Button?>(R.id.controllerBtnPlayPause).apply {
            setOnClickListener {
                viewModel.playPauseCurrentTrack()
            }
        }

        stopButton = view.findViewById<Button?>(R.id.controllerBtnStop).apply {
            setOnClickListener {
                viewModel.stopCurrentTrack()
            }
        }


        // The progress of the seekbar needs to be synced with the currentPosition of the MediaPlayer
        // when transitioning from ADD_PLAYLIST back to PLAY_MUSIC

        controllerSeekBar = view.findViewById<SeekBar?>(R.id.controllerSeekBar).apply {
            progress = viewModel.mediaPlayer.currentPosition / 1000

            setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    controllerCurrentTimeTextView.text = progress.durationSecondsString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // Stop the MediaPlayer current position from changing the progress of the seekbar
                    handler.removeCallbacks(syncControllerSeekBarProgress)
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    viewModel.updateMediaPlayerCurrentPosition(progress * 1000)
                    handler.post(syncControllerSeekBarProgress)
                }
            })
        }

        controllerCurrentTimeTextView = view.findViewById(R.id.controllerTvCurrentTime)
        controllerTotalTimeTextView = view.findViewById(R.id.controllerTvTotalTime)
    }

    override fun onStart() {
        super.onStart()
        handler.post(syncControllerSeekBarProgress)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(syncControllerSeekBarProgress)
    }
}