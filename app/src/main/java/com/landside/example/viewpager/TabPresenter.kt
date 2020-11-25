package com.landside.example.viewpager

import androidx.lifecycle.LifecycleOwner
import com.landside.example.share.MainShareAgent
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.InjectAgent

class TabPresenter(val owner: LifecycleOwner) {

  init {
    ShadowState.injectDispatcher(this,owner)
  }

  @InjectAgent
  lateinit var shareAgent: MainShareAgent

  fun changeName(){
    shareAgent.setState { it.copy(
        shareName = "shareName" + shareAgent.state.shareCount,
        shareCount = shareAgent.state.shareCount+1,
        item = shareAgent.state.item.copy(
            data = "item"+ shareAgent.state.shareCount
        )
    ) }
  }

}