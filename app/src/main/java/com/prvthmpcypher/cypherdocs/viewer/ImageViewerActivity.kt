package com.prvthmpcypher.cypherdocs.viewer

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import com.prvthmpcypher.cypherdocs.R
import com.prvthmpcypher.cypherdocs.widget.ZoomableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                    contentResolver.openInputStream(fileUri)?.use { BitmapFactory.decodeStream(it) }
                } catch (e: Exception) {
                    null
                }
            }
            progressBar.visibility = View.GONE
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
                imageView.post { imageView.resetZoom() }
            }
        }
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
