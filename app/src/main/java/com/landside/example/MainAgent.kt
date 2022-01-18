package com.landside.example

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.landside.example.share.MainShareAgent
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate.StateAgent
import com.landside.shadowstate_annotation.InjectAgent

class MainAgent : StateAgent<MainState, MainView>() {

    @InjectAgent
    lateinit var shareAgent: MainShareAgent

    override fun initState(bundle: Bundle?): MainState =
        MainState(name = "hahah",listStates = listOf("1","2"))

    override fun conf() {
        view?.let {
            ShadowState.injectDispatcher(this, it as LifecycleOwner)
        }
        listen(false,{ it.name }, {
            view?.setName("$it${state.amendInternal}  gender:${state.childState.gender}")
//            setState { it.copy(amendInternal = it.amendInternal+1) }
        })
        listen({it.listStates},{
            view?.setListContents(it)
        })
    }
}