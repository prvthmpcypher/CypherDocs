package com.prvthmpcypher.cypherdocs.widget

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

/**
 * Minimal, dependency-free zoomable ImageView:
 *  - Pinch to zoom
 *  - Double-tap to zoom in/reset
 *  - Drag to pan while zoomed
 *  - Public zoomIn()/zoomOut()/resetZoom() for toolbar buttons
 */
class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val matrixValues = FloatArray(9)
    private val imageMatrix2 = Matrix()

    private var minScale = 1f
    private var maxScale = 6f
    private var currentScale = 1f

    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector

    private var lastFocusX = 0f
    private var lastFocusY = 0f
    private var isDragging = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    init {
        scaleType = ScaleType.MATRIX
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
        gestureDetector = GestureDetector(context, GestureListener())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetZoom()
    }

    fun resetZoom() {
        imageMatrix2.reset()
        val d = drawable ?: return
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val drawableWidth = d.intrinsicWidth.toFloat()
        val drawableHeight = d.intrinsicHeight.toFloat()
        if (viewWidth <= 0 || viewHeight <= 0 || drawableWidth <= 0 || drawableHeight <= 0) return

        val scale = min(viewWidth / drawableWidth, viewHeight / drawableHeight)
        val dx = (viewWidth - drawableWidth * scale) / 2f
        val dy = (viewHeight - drawableHeight * scale) / 2f

        imageMatrix2.setScale(scale, scale)
        imageMatrix2.postTranslate(dx, dy)
        currentScale = 1f
        minScale = 1f
        imageMatrix = imageMatrix2
    }

    fun zoomIn() = applyStepScale(1.4f)
    fun zoomOut() = applyStepScale(1f / 1.4f)

    private fun applyStepScale(factor: Float) {
        val newScale = (currentScale * factor).coerceIn(minScale, maxScale)
        val actualFactor = newScale / currentScale
        currentScale = newScale
        imageMatrix2.postScale(actualFactor, actualFactor, width / 2f, height / 2f)
        imageMatrix = imageMatrix2
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && currentScale > minScale) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    imageMatrix2.postTranslate(dx, dy)
                    imageMatrix = imageMatrix2
                    lastTouchX = event.x
                    lastTouchY = event.y
                    isDragging = true
                }
            }
        }
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            lastFocusX = detector.focusX
            lastFocusY = detector.focusY
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val factor = detector.scaleFactor
            val newScale = (currentScale * factor).coerceIn(minScale, maxScale)
            val actualFactor = if (currentScale == 0f) 1f else newScale / currentScale
            currentScale = newScale
            imageMatrix2.postScale(actualFactor, actualFactor, detector.focusX, detector.focusY)
            imageMatrix = imageMatrix2
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (currentScale > minScale + 0.05f) {
                resetZoom()
            } else {
                val targetScale = min(maxScale, minScale * 2.5f)
                val factor = targetScale / currentScale
                currentScale = targetScale
                imageMatrix2.postScale(factor, factor, e.x, e.y)
                imageMatrix = imageMatrix2
            }
            return true
        }
    }
}
