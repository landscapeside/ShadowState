package com.landside.shadowstate.example

import android.app.Application
import com.landside.shadowstate.BuildConfig
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate.ShadowStateManager

class TestApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ShadowState.install(
            ShadowStateManager(),
            BuildConfig.DEBUG
        )
    }
}