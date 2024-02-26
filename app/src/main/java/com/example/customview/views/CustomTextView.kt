package com.example.customview.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CustomTextView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet
) : View(context, attr) {

    private val paint = Paint().also {
        it.style = Paint.Style.FILL
        it.textAlign = Paint.Align.CENTER
        it.color = Color.GRAY
        it.textSize = 50f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawText("It's a text.", (width / 2).toFloat(), (height / 2).toFloat(), paint)
    }

    fun makeVisible() {
        visibility = VISIBLE
    }

    fun makeGone() {
        visibility = GONE
    }
}