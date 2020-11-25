package com.landside.example

import androidx.lifecycle.LifecycleOwner
import com.landside.example.share.MainShareAgent
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.InjectAgent

class SecondPresenter(val owner: LifecycleOwner) {

    init {
        ShadowState.injectDispatcher(this,owner)
    }

    @InjectAgent
    lateinit var agent: CloneMainAgent

    @InjectAgent
    lateinit var shareAgent: MainShareAgent


    fun changeName(){
        agent.setState { it.copy(name = it.name+"222222") }
        shareAgent.setState { it.copy(
            shareName = "shareName" + shareAgent.state.shareCount,
            shareCount = shareAgent.state.shareCount+1,
            item = shareAgent.state.item.copy(
                data = "item"+ shareAgent.state.shareCount
            )
        ) }
    }
}