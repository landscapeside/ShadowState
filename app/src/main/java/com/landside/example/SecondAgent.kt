package com.landside.example

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.landside.example.share.MainShareAgent
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate.StateAgent
import com.landside.shadowstate_annotation.InjectAgent

class SecondAgent:StateAgent<SecondState,SecondActivity>() {

    @InjectAgent
    lateinit var shareAgent: MainShareAgent

    override fun initState(bundle: Bundle?): SecondState =
        SecondState()

    override fun conf() {
        view?.let {
            ShadowState.injectDispatcher(this, it as LifecycleOwner)
        }
        listen({it.name},{
            view?.setName(it)
        })
    }
}