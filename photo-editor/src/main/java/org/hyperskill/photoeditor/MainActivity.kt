package org.hyperskill.photoeditor

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.core.graphics.blue
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.google.android.material.slider.Slider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.pow

private const val PERMISSION_REQUEST_CODE = 0

class MainActivity : AppCompatActivity() {

    private lateinit var currentImage: ImageView
    private lateinit var galleryButton: Button
    private lateinit var saveButton: Button
    private lateinit var brightnessSlider: Slider
    private lateinit var contrastSlider: Slider
    private lateinit var saturationSlider: Slider
    private lateinit var gammaSlider: Slider
    private lateinit var baseBitmap: Bitmap
    private lateinit var editBitmap: Bitmap
    private var brightness = 0
    private var contrast = 0
    private var saturation = 0
    private var gamma = 0.0

    @Volatile
    private lateinit var applyFilterJob: Job

    private val activityResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photoUri = result.data?.data ?: return@registerForActivityResult
            // code to update ivPhoto with loaded image
            currentImage.setImageURI(photoUri)
            baseBitmap = currentImage.drawable.toBitmap()
            editBitmap = baseBitmap.copy(Bitmap.Config.RGB_565, true)
        }
    }

    private fun bindViews() {
        currentImage = findViewById(R.id.ivPhoto)
        galleryButton = findViewById(R.id.btnGallery)
        saveButton = findViewById(R.id.btnSave)
        brightnessSlider = findViewById(R.id.slBrightness)
        contrastSlider = findViewById(R.id.slContrast)
        saturationSlider = findViewById(R.id.slSaturation)
        gammaSlider = findViewById(R.id.slGamma)
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else {
            PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED
        }
    }

    private fun Int.clampPixelValue() = this.coerceAtLeast(0).coerceAtMost(255)
    private fun Double.clampPixelValue() = this.toInt().clampPixelValue()

    private fun calculateAverageBrightness(pixels: IntArray): Int {
        var sumBrightness = 0L
        for (pixel in pixels) {
            sumBrightness += pixel.red + pixel.green + pixel.blue
        }
        return (sumBrightness / (pixels.size * 3)).toInt()
    }

    private fun calculatePixelBrightness(pixel: Int) = (pixel + brightness).clampPixelValue()
    private fun calculatePixelContrast(alpha: Double, pixel: Int, averageBrightness: Int) = (alpha * (pixel - averageBrightness) + averageBrightness).clampPixelValue()
    private fun calculatePixelSaturation(alpha: Double, pixel: Int, averageRGB: Int) = (alpha * (pixel - averageRGB) + averageRGB).clampPixelValue()
    private fun calculatePixelGamma(pixel: Int) = (255 * (pixel.toDouble() / 255).pow(gamma)).clampPixelValue()

    private fun applyBrightness(pixels: IntArray) {
        for (i in pixels.indices) {
            val r = calculatePixelBrightness(pixels[i].red)
            val g = calculatePixelBrightness(pixels[i].green)
            val b = calculatePixelBrightness(pixels[i].blue)
            pixels[i] = Color.rgb(r, g, b)
        }
    }

    private fun applyContrast(pixels: IntArray) {
        val averageBrightness = calculateAverageBrightness(pixels)
        val alpha = (255 + contrast) / (255 - contrast).toDouble()
        for (i in pixels.indices) {
            val r = calculatePixelContrast(alpha, pixels[i].red, averageBrightness)
            val g = calculatePixelContrast(alpha, pixels[i].green, averageBrightness)
            val b = calculatePixelContrast(alpha, pixels[i].blue, averageBrightness)
            pixels[i] = Color.rgb(r, g, b)
        }
    }

    private fun applySaturation(pixels: IntArray) {
        val alpha = (255 + saturation) / (255 - saturation).toDouble()
        for (i in pixels.indices) {
            val averageRGB = (pixels[i].red + pixels[i].green + pixels[i].blue) / 3
            val r = calculatePixelSaturation(alpha, pixels[i].red, averageRGB)
            val g = calculatePixelSaturation(alpha, pixels[i].green, averageRGB)
            val b = calculatePixelSaturation(alpha, pixels[i].blue, averageRGB)
            pixels[i] = Color.rgb(r, g, b)
        }
    }

    private fun applyGamma(pixels: IntArray) {
        for (i in pixels.indices) {
            val r = calculatePixelGamma(pixels[i].red)
            val g = calculatePixelGamma(pixels[i].green)
            val b = calculatePixelGamma(pixels[i].blue)
            pixels[i] = Color.rgb(r, g, b)
        }
    }

    suspend private fun applyFilters() {
        val width = baseBitmap.width
        val height = baseBitmap.height
        val pixels = IntArray(width * height)

        baseBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        applyBrightness(pixels)
        applyContrast(pixels)
        applySaturation(pixels)
        applyGamma(pixels)

        runOnUiThread {
            editBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            currentImage.setImageBitmap(editBitmap)
        }
    }

    private fun startApplyFilterJob(changeFilterValue: () -> Unit) {
        changeFilterValue()

        if (::applyFilterJob.isInitialized) {
            applyFilterJob.cancel()
        }

        applyFilterJob = CoroutineScope(Dispatchers.Default).launch {
            applyFilters()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) saveButton.callOnClick()
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()

        baseBitmap = createBitmap()
        currentImage.setImageBitmap(baseBitmap)
        editBitmap = baseBitmap.copy(Bitmap.Config.RGB_565, true)

        brightness = brightnessSlider.value.toInt()
        contrast = contrastSlider.value.toInt()
        saturation = saturationSlider.value.toInt()
        gamma = gammaSlider.value.toDouble()

        galleryButton.setOnClickListener {
            activityResultLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
        }

        saveButton.setOnClickListener {
            if (hasPermission()) {
                val savedImagesUri = if (Build.VERSION.SDK_INT >= 29) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL) else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val savedImage = ContentValues().apply {
                    put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.ImageColumns.WIDTH, editBitmap.width)
                    put(MediaStore.Images.ImageColumns.HEIGHT, editBitmap.height)
                }
                val savedImageUri = contentResolver.insert(savedImagesUri, savedImage)
                        ?: return@setOnClickListener

                contentResolver.openOutputStream(savedImageUri).use {
                    editBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
            }
        }

        brightnessSlider.addOnChangeListener { _, brightness, _ ->
            startApplyFilterJob { this.brightness = brightness.toInt() }
        }

        contrastSlider.addOnChangeListener { _, contrast, _ ->
            startApplyFilterJob { this.contrast = contrast.toInt() }
        }

        saturationSlider.addOnChangeListener { _, saturation, _ ->
            startApplyFilterJob { this.saturation = saturation.toInt() }
        }

        gammaSlider.addOnChangeListener { _, gamma, _ ->
            startApplyFilterJob { this.gamma = gamma.toDouble() }
        }
    }

    // do not change this function
    fun createBitmap(): Bitmap {
        val width = 200
        val height = 100
        val pixels = IntArray(width * height)
        // get pixel array from source

        var R: Int
        var G: Int
        var B: Int
        var index: Int

        for (y in 0 until height) {
            for (x in 0 until width) {
                // get current index in 2D-matrix
                index = y * width + x
                // get color
                R = x % 100 + 40
                G = y % 100 + 80
                B = (x + y) % 100 + 120

                pixels[index] = Color.rgb(R, G, B)

            }
        }
        // output bitmap
        val bitmapOut = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmapOut.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmapOut
    }
}