package com.example.customview

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import coil.load
import coil.memory.MemoryCache
import com.example.customview.CustomViewApplication.Companion.imageStatus
import com.example.customview.views.CustomTextView
import com.example.customview.views.CustomWheelView
import com.example.customview.views.WheelValue

class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalCoilApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<CustomWheelView>(R.id.customWheelView).currentWheelValue.observe(this) {
            when (it) {
                WheelValue.TEXT -> findViewById<CustomTextView>(R.id.customTextView).makeVisible()
                WheelValue.PICTURE -> {
                    this.imageLoader.diskCache?.remove("https://placebeard.it/640x360")
                    this.imageLoader.memoryCache?.remove(MemoryCache.Key("https://placebeard.it/640x360"))
                    findViewById<ImageView>(R.id.imageView).load("https://placebeard.it/640x360")
                    imageStatus = ImageStatus.LOADED
                }

                else -> {}
            }
        }

        findViewById<Button>(R.id.button).setOnClickListener {
            this.imageLoader.diskCache?.remove("https://placebeard.it/640x360")
            this.imageLoader.memoryCache?.remove(MemoryCache.Key("https://placebeard.it/640x360"))
            findViewById<ImageView>(R.id.imageView).setImageResource(android.R.color.transparent)
            findViewById<CustomTextView>(R.id.customTextView).makeGone()
            imageStatus = ImageStatus.NONE
        }

        findViewById<SeekBar>(R.id.seekBar).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                findViewById<CustomWheelView>(R.id.customWheelView).rescale(progress.toFloat() / 100f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putBoolean("isVisibleCustomTextView", findViewById<CustomTextView>(R.id.customTextView).isVisible)
        savedInstanceState.putString("imageStatus", imageStatus.name)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        if (savedInstanceState.getBoolean("isVisibleCustomTextView"))
            findViewById<CustomTextView>(R.id.customTextView).makeVisible()

        if(savedInstanceState.getString("imageStatus") == ImageStatus.LOADED.name)
            findViewById<ImageView>(R.id.imageView).load("https://placebeard.it/640x360")
    }
}
