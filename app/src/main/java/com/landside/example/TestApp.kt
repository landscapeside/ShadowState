package com.landside.example

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.landside.shadowstate.BuildConfig
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate.ZipStateManager
import com.landside.submodule.SubStateManager

class TestApp : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        ZipStateManager.zip(MainStateManager(),SubStateManager())
        ShadowState.init(
            applicationContext,
            BuildConfig.DEBUG,
            true
        )
    }
}