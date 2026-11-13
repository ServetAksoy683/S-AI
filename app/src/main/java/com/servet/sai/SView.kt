package com.servet.sai

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View

class SView(c: Context) : View(c) {
    private val pt = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 15f
    }
    private val path = Path().apply {
        moveTo(78f, 24f)
        cubicTo(70f, 8f, 30f, 4f, 24f, 28f)
        cubicTo(18f, 54f, 78f, 46f, 78f, 72f)
        cubicTo(78f, 96f, 30f, 96f, 20f, 74f)
    }
    private var anim: ObjectAnimator? = null

    override fun onDraw(cv: Canvas) {
        val sz = minOf(width, height).toFloat() * 0.9f
        cv.translate((width - sz) / 2f, (height - sz) / 2f)
        cv.scale(sz / 100f, sz / 100f)
        cv.drawPath(path, pt)
    }

    fun active(on: Boolean) {
        anim?.cancel()
        alpha = 1f
        if (on) {
            anim = ObjectAnimator.ofFloat(this, "alpha", 1f, 0.3f).apply {
                duration = 700
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
                start()
            }
        }
    }
}
