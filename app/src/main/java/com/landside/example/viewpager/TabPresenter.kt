package com.landside.example.viewpager

import androidx.lifecycle.LifecycleOwner
import com.landside.example.share.MainShareAgent
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.InjectAgent
import kotlin.random.Random

class TabPresenter(val owner: LifecycleOwner) {

  init {
    ShadowState.injectDispatcher(this,owner)
  }

  @InjectAgent
  lateinit var shareAgent: MainTagShareAgent
  @InjectAgent
  lateinit var attachAgent:ActivityAttachAgent
  @InjectAgent
  lateinit var attachAgent2: ActivityAttachAgent2

  fun changeName(){
    shareAgent.setState { it.copy(
        shareName = "shareName" + shareAgent.state.shareCount,
        shareCount = shareAgent.state.shareCount+1,
        item = shareAgent.state.item.copy(
            data = "item"+ shareAgent.state.shareCount
        )
    ) }
    attachAgent.setState { it.copy(
        name = ('a'..'z').random().toString()
    ) }
    attachAgent2.setState { it.copy(
        age = Random.nextInt()
    ) }
  }

}