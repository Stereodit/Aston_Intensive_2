package com.example.customview.views

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.lifecycle.MutableLiveData
import java.math.RoundingMode


class CustomWheelView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet
) : View(context, attr) {

    private val paint = Paint().also { it.style = Paint.Style.FILL }
    private val colors = listOf(
        Pair(Color.RED, WheelValue.TEXT),
        Pair(Color.rgb(255, 165, 0), WheelValue.PICTURE),
        Pair(Color.YELLOW, WheelValue.TEXT),
        Pair(Color.GREEN, WheelValue.PICTURE),
        Pair(Color.rgb(0, 191, 255), WheelValue.TEXT),
        Pair(Color.BLUE, WheelValue.PICTURE),
        Pair(Color.MAGENTA, WheelValue.TEXT),
    )

    private var startAngle = -90f
    private val sweepAngle = 360f / colors.size

    private var randomSweepAngle = 0f
    private var scale = 0.5f

    val currentWheelValue: MutableLiveData<WheelValue> by lazy {
        MutableLiveData<WheelValue>()
    }

    private fun getWheelValue() : WheelValue {
        return colors[(randomSweepAngle % 360f / sweepAngle).toBigDecimal().setScale(0, RoundingMode.DOWN).toInt()].second
    }

    init {
        this.isClickable = true
    }

    fun rescale(scale: Float) {
        this.scale = 1 - scale
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCircle(canvas)
    }

    private fun drawCircle(canvas: Canvas) {
        val radius = width.coerceAtMost(height) / 2f
        val centerX = width / 2f
        val centerY = height / 2f
        val scaleValue = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 120f else 80f

        repeat(colors.size) {
            canvas.drawArc(
                centerX - radius + scaleValue * scale,
                centerY - radius + scaleValue * scale,
                centerX + radius - scaleValue * scale,
                centerY + radius - scaleValue * scale,
                startAngle + it * sweepAngle + randomSweepAngle,
                sweepAngle,
                true,
                paint.also { paint ->  paint.color = colors[it].first }
            )
        }

        canvas.drawArc(
            centerX - 100f,
            centerY - radius - 10f + scaleValue * scale,
            centerX + 100f,
            centerY - radius + 100f + scaleValue * scale,
            startAngle - sweepAngle / 2,
            sweepAngle,
            true,
            paint.also { paint ->  paint.color = Color.BLACK }
        )
    }

    private fun spinWheel() {
        if (randomSweepAngle != 0f) backWheel()

        val animator = ValueAnimator.ofFloat(0f, (1081..2160).random().toFloat())
        animator.duration = 5000
        animator.startDelay = 1000
        animator.doOnEnd {
            this.isClickable = true
            currentWheelValue.value = getWheelValue()
        }
        animator.start()

        animator.addUpdateListener {
            randomSweepAngle = animator.animatedValue as Float
            invalidate()
        }
    }

    private fun backWheel() {
        val animator = ValueAnimator.ofFloat(randomSweepAngle, 2160f)
        animator.duration = 500
        animator.doOnEnd { randomSweepAngle = 0f }
        animator.start()

        animator.addUpdateListener {
            randomSweepAngle = animator.animatedValue as Float
            invalidate()
        }
    }

    override fun performClick(): Boolean {
        spinWheel()
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_UP -> {
                if(isClickable) {
                    isClickable = false
                    performClick()
                }
            }
        }
        return true
    }
}