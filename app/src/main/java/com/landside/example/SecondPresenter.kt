package com.landside.example

import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.InjectAgent

class SecondPresenter {

    init {
        ShadowState.injectDispatcher(this)
    }

    @InjectAgent
    lateinit var agent: SecondAgent


    fun changeName(){
        agent.setState { it.copy(name = it.name+"++") }
    }
}