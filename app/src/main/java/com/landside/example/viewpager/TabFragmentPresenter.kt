package com.landside.example.viewpager

import androidx.lifecycle.LifecycleOwner
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.InjectAgent
import kotlin.random.Random

class TabFragmentPresenter(val owner: LifecycleOwner) {
  init {
    ShadowState.injectDispatcher(this,owner)
  }

  @InjectAgent
  lateinit var attachAgent:FragmentAttachAgent

  fun change(){
    attachAgent.setState {
      it.copy(
          name = ('a'..'z').random().toString()
      )
    }
  }
}