package org.hyperskill.musicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

const val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1

class MainActivity : AppCompatActivity() {
    private val viewModel: MusicPlayerViewModel by viewModels {
        MusicPlayerViewModel.provideFactory(this)
    }
    private lateinit var searchButton: Button
    private lateinit var songList: RecyclerView
    private lateinit var fragmentContainer: FragmentContainerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()

        viewModel.init()

        searchButton.setOnClickListener {
            if (hasReadExternalStoragePermission()) {
                viewModel.search()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mainMenuAddPlaylist -> {
                viewModel.addPlaylistFromMenu()
                true
            }

            R.id.mainMenuLoadPlaylist -> {
                viewModel.loadPlaylistFromMenu()
                true
            }

            R.id.mainMenuDeletePlaylist -> {
                viewModel.deletePlaylistFromMenu()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun notifyAdapterItemChangedNoAnimation(position: Int) {
        songList.adapter?.notifyItemChanged(position, Unit)
    }

    fun setAdapter(adapter: RecyclerView.Adapter<ViewHolder>) {
        songList.adapter = adapter
    }

    private fun bindViews() {
        searchButton = findViewById(R.id.mainButtonSearch)
        songList = findViewById(R.id.mainSongList)
        fragmentContainer = findViewById(R.id.mainFragmentContainer)
    }

    private fun hasReadExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            PermissionChecker.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PermissionChecker.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    viewModel.search()
                } else {
                    Toast.makeText(
                        this,
                        "Songs cannot be loaded without permission",
                        Toast.LENGTH_LONG
                    ).show()
                }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}