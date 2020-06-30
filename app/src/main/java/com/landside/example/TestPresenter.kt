package com.landside.example

import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.InjectAgent

class TestPresenter {

    init {
        ShadowState.injectDispatcher(this)
    }

    @InjectAgent
    lateinit var agent: TestAgent


    fun changeName(){
        agent.setState { it.copy(name = it.name+"++") }
    }
}