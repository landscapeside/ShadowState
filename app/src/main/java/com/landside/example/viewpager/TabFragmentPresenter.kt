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
  @InjectAgent
  lateinit var attachAgent2: FragmentAttachAgent2

  fun change(){
    attachAgent.setState {
      it.copy(
          name = ('a'..'z').random().toString()
      )
    }
    attachAgent2.setState {
      it.copy(
          age = Random.nextInt()
      )
    }
  }
}