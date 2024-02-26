package com.example.customview

import android.app.Application

class CustomViewApplication : Application() {
    companion object {
        var imageStatus = ImageStatus.NONE
    }
}