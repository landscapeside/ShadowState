package com.landside.example

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout

class TestViewGroup(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d("TestViewGroup","onAttachedToWindow")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d("TestViewGroup","onDetachedFromWindow")
    }
}