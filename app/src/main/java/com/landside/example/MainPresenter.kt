package com.landside.example

import androidx.lifecycle.LifecycleOwner
import com.landside.example.share.MainShareAgent
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.InjectAgent

class MainPresenter(val owner: LifecycleOwner) {
  init {
    ShadowState.injectDispatcher(this, owner)
  }
  @InjectAgent
  lateinit var agent: MainAgent
  @InjectAgent
  lateinit var shareAgent: MainShareAgent

  fun changeName() {
    agent.setState { it.copy(name = it.name + "++") }
    shareAgent.setState {
      it.copy(
          shareName = "shareName" + shareAgent.state.shareCount,
          shareCount = shareAgent.state.shareCount + 1,
          item = shareAgent.state.item.copy(
              data = "item" + shareAgent.state.shareCount
          )
      )
    }
  }
}