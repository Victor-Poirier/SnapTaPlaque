package com.example.snaptaplaque.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.min

class DashBoardGauge @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var isSelectedGauge = false
        set(value) {
            field = value
            invalidate()
        }

    var tabIndex = 0
        set(value) {
            field = value
            invalidate()
        }

    // Icône centrale
    var iconResId: Int = 0
        set(value) {
            field = value
            iconDrawable = if (value != 0)
                ContextCompat.getDrawable(context, value)
            else null
            invalidate()
        }

    private var iconDrawable: Drawable? = null
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = 8f
        circlePaint.color = Color.RED
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val radius = min(width, height) / 2f - 16f
        if (radius <= 0) return

        val cx = width / 2f
        val cy = height / 2f

        circlePaint.color = if (isSelectedGauge) Color.WHITE else Color.RED
        circlePaint.strokeWidth = if (isSelectedGauge) 14f else 8f

        canvas.drawCircle(cx, cy, radius, circlePaint)

        // Dessin icône
        iconDrawable?.let { drawable ->

            val iconSize = (radius * 1.0f).toInt()

            val left = (cx - iconSize / 2f).toInt()
            val top = (cy - iconSize / 2f).toInt()
            val right = (cx + iconSize / 2f).toInt()
            val bottom = (cy + iconSize / 2f).toInt()

            drawable.setBounds(left, top, right, bottom)

            drawable.setTint(Color.WHITE)
            drawable.alpha = if (isSelectedGauge) 255 else 160

            drawable.draw(canvas)
        }
    }
}