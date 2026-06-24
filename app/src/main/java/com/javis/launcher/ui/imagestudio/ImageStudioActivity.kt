package com.javis.launcher.ui.imagestudio

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.javis.launcher.databinding.ActivityImageStudioBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ImageStudioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageStudioBinding
    private var currentBitmap: Bitmap? = null
    private var currentUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        currentUri = uri
        Glide.with(this).load(uri).into(binding.ivPreview)
        lifecycleScope.launch(Dispatchers.IO) {
            currentBitmap = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageStudioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Image Studio"

        binding.btnPickImage.setOnClickListener { pickImage.launch("image/*") }
        binding.btnBrightness.setOnClickListener { applyFilter("brightness") }
        binding.btnContrast.setOnClickListener { applyFilter("contrast") }
        binding.btnGrayscale.setOnClickListener { applyFilter("grayscale") }
        binding.btnSave.setOnClickListener { saveImage() }
    }

    private fun applyFilter(type: String) {
        val bmp = currentBitmap ?: run { Toast.makeText(this, "Pick an image first", Toast.LENGTH_SHORT).show(); return }
        lifecycleScope.launch {
            val result = withContext(Dispatchers.Default) {
                when (type) {
                    "grayscale" -> toGrayscale(bmp)
                    "brightness" -> adjustBrightness(bmp, 40)
                    "contrast" -> adjustContrast(bmp, 1.3f)
                    else -> bmp
                }
            }
            currentBitmap = result
            binding.ivPreview.setImageBitmap(result)
        }
    }

    private fun toGrayscale(src: Bitmap): Bitmap {
        val result = src.copy(Bitmap.Config.ARGB_8888, true)
        for (x in 0 until result.width) for (y in 0 until result.height) {
            val px = result.getPixel(x, y)
            val r = android.graphics.Color.red(px); val g = android.graphics.Color.green(px); val b = android.graphics.Color.blue(px)
            val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            result.setPixel(x, y, android.graphics.Color.rgb(gray, gray, gray))
        }
        return result
    }

    private fun adjustBrightness(src: Bitmap, value: Int): Bitmap {
        val result = src.copy(Bitmap.Config.ARGB_8888, true)
        for (x in 0 until result.width) for (y in 0 until result.height) {
            val px = result.getPixel(x, y)
            val r = (android.graphics.Color.red(px) + value).coerceIn(0, 255)
            val g = (android.graphics.Color.green(px) + value).coerceIn(0, 255)
            val b = (android.graphics.Color.blue(px) + value).coerceIn(0, 255)
            result.setPixel(x, y, android.graphics.Color.rgb(r, g, b))
        }
        return result
    }

    private fun adjustContrast(src: Bitmap, factor: Float): Bitmap {
        val result = src.copy(Bitmap.Config.ARGB_8888, true)
        for (x in 0 until result.width) for (y in 0 until result.height) {
            val px = result.getPixel(x, y)
            val r = ((factor * (android.graphics.Color.red(px) - 128) + 128)).toInt().coerceIn(0,255)
            val g = ((factor * (android.graphics.Color.green(px) - 128) + 128)).toInt().coerceIn(0,255)
            val b = ((factor * (android.graphics.Color.blue(px) - 128) + 128)).toInt().coerceIn(0,255)
            result.setPixel(x, y, android.graphics.Color.rgb(r, g, b))
        }
        return result
    }

    private fun saveImage() {
        val bmp = currentBitmap ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val file = File(cacheDir, "javis_edit_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            withContext(Dispatchers.Main) { Toast.makeText(this@ImageStudioActivity, "Image saved!", Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}
