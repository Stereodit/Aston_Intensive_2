package com.example.customview.views

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.animation.doOnCancel
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

    private enum class AnimationStatus {
        NONE, BACK_SPIN, SPIN
    }

    private var currentAnimationStatus = AnimationStatus.NONE
    private var currentAnimationTime : Long? = 0
    private var spinTo : Float? = 0f

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

    override fun onAttachedToWindow() {
        when (currentAnimationStatus) {
            AnimationStatus.BACK_SPIN -> {
                Log.d("TAG", "BACK_SPIN INIT")
                refreshAndSpinWheel(currentAnimationTime ?: 0)
            }
            AnimationStatus.SPIN -> {
                Log.d("TAG", "SPIN INIT")
                spinWheel(currentAnimationTime ?: 0, spinTo ?: 0f)
            }
            else -> { Log.d("TAG", "NONE INIT")  }
        }

        super.onAttachedToWindow()
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

    private fun refreshAndSpinWheel(currentAnimationTime: Long = 0) {
        if (randomSweepAngle != 0f) {
            currentAnimationStatus = AnimationStatus.BACK_SPIN

            val animator = ValueAnimator.ofFloat(randomSweepAngle, 2160f)
            animator.duration = 2000 - currentAnimationTime
            animator.doOnEnd {
                randomSweepAngle = 0f
                spinWheel()
            }
            animator.doOnCancel {
                currentAnimationStatus = AnimationStatus.NONE
            }
            animator.start()

            animator.addUpdateListener {
                randomSweepAngle = animator.animatedValue as Float
                this.currentAnimationTime = animator.currentPlayTime
                invalidate()
            }
        } else spinWheel()
    }

    private fun spinWheel(currentAnimationTime: Long = 0, spinTo: Float = 0f) {
        currentAnimationStatus = AnimationStatus.SPIN

        val newSpinTo = if (spinTo != 0f) spinTo else (1081..2160).random().toFloat()
        this.spinTo = newSpinTo

        val animator = ValueAnimator.ofFloat(if (spinTo != 0f) randomSweepAngle else 0f, newSpinTo)
        animator.duration = 5000 - currentAnimationTime
        animator.startDelay = if (spinTo != 0f) 0 else 1000
        animator.doOnEnd {
            this.isClickable = true
            currentWheelValue.value = getWheelValue()
            currentAnimationStatus = AnimationStatus.NONE
        }
        animator.doOnCancel { currentAnimationStatus = AnimationStatus.NONE }
        animator.start()

        animator.addUpdateListener {
            randomSweepAngle = animator.animatedValue as Float
            this.currentAnimationTime = animator.currentPlayTime
            invalidate()
        }
    }

    override fun performClick(): Boolean {
        refreshAndSpinWheel()
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

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putString("animationStatus", currentAnimationStatus.name)
        bundle.putFloat("randomSweepAngle", randomSweepAngle)
        bundle.putLong("currentAnimationTime", currentAnimationTime ?: 0)
        bundle.putFloat("spinTo", spinTo ?: 0f)
        bundle.putParcelable("instanceState", super.onSaveInstanceState())
        return bundle
    }


    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as Bundle
        currentAnimationStatus = when (state.getString("animationStatus")) {
            AnimationStatus.BACK_SPIN.toString() -> AnimationStatus.BACK_SPIN
            AnimationStatus.SPIN.toString() -> AnimationStatus.SPIN
            else -> AnimationStatus.NONE
        }
        randomSweepAngle = state.getFloat("randomSweepAngle")
        currentAnimationTime = state.getLong("currentAnimationTime")
        spinTo = state.getFloat("spinTo")
        super.onRestoreInstanceState(bundle.getParcelable("instanceState"))
    }
}