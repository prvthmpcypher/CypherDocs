package com.prvthmpcypher.cypherdocs.viewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.prvthmpcypher.cypherdocs.R
import com.prvthmpcypher.cypherdocs.widget.ZoomableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class ImageViewerActivity : BaseViewerActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var imageView: ZoomableImageView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)
        readCommonExtras()

        toolbar = findViewById(R.id.toolbar)
        toolbar.title = fileName
        toolbar.setNavigationOnClickListener { finish() }
        setSupportActionBar(toolbar)

        imageView = findViewById(R.id.zoomable_image_view)
        progressBar = findViewById(R.id.progress_bar)

        findViewById<ImageButton>(R.id.button_zoom_in).setOnClickListener { imageView.zoomIn() }
        findViewById<ImageButton>(R.id.button_zoom_out).setOnClickListener { imageView.zoomOut() }

        setupPageNavigation(
            findViewById(R.id.button_prev_file),
            findViewById(R.id.button_next_file),
            findViewById(R.id.text_page_indicator)
        )

        loadImage()
    }

    private fun loadImage() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    decodeSampledBitmap()
                } catch (e: OutOfMemoryError) {
                    null
                } catch (e: Exception) {
                    null
                }
            }
            progressBar.visibility = View.GONE
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
                imageView.post { imageView.resetZoom() }
            } else {
                Toast.makeText(this@ImageViewerActivity, getString(R.string.file_open_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Decodes the image downsampled to roughly the device's display size instead of at full
     * resolution. Modern camera photos (12–100+ MP) decoded at full size can easily be
     * 100+ MB as an in-memory ARGB_8888 bitmap, which reliably OOM-crashes on real devices.
     * We read the bounds first (no pixel data loaded), compute a power-of-two inSampleSize
     * that fits a generous target (2x screen size, so pinch-zoom still looks sharp), then
     * decode once for real at that size.
     */
    private fun decodeSampledBitmap(): Bitmap? {
        val metrics: DisplayMetrics = resources.displayMetrics
        val reqWidth = (metrics.widthPixels * 2).coerceAtMost(4096)
        val reqHeight = (metrics.heightPixels * 2).coerceAtMost(4096)

        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        openFileStream()?.use { BitmapFactory.decodeStream(it, null, boundsOptions) } ?: return null

        val sampleSize = calculateInSampleSize(boundsOptions.outWidth, boundsOptions.outHeight, reqWidth, reqHeight)
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return openFileStream()?.use { BitmapFactory.decodeStream(it, null, decodeOptions) }
    }

    private fun openFileStream(): InputStream? = contentResolver.openInputStream(fileUri)

    private fun calculateInSampleSize(rawWidth: Int, rawHeight: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (rawWidth <= 0 || rawHeight <= 0) return inSampleSize
        if (rawHeight > reqHeight || rawWidth > reqWidth) {
            var halfHeight = rawHeight / 2
            var halfWidth = rawWidth / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_image_viewer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                shareCurrentFile()
                true
            }
            R.id.action_browse -> {
                startActivity(android.content.Intent(this, com.prvthmpcypher.cypherdocs.MainActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
