package com.landside.shadowstate

import androidx.lifecycle.LifecycleOwner

object ZipStateManager : StateManager {

  internal val managers = mutableListOf<StateManager>()

  fun zip(vararg managers: StateManager) {
    this.managers.addAll(managers)
  }

  override fun getStateWrapInfo(lifecycleOwner: LifecycleOwner?): StateWrapInfo? {
    var stateWrapInfo: StateWrapInfo? = null
    managers.forEach {
      if (it.getStateWrapInfo(lifecycleOwner) != null) {
        stateWrapInfo = it.getStateWrapInfo(lifecycleOwner)
        return@forEach
      }
    }
    return stateWrapInfo
  }

  override fun bind(lifecycleOwner: LifecycleOwner?) {
    managers.forEach {
      it.bind(lifecycleOwner)
    }
  }

  override fun getStateAgent(lifecycleOwner: LifecycleOwner?): StateAgent<*, *>? {
    var agent: StateAgent<*, *>? = null
    managers.forEach {
      if (it.getStateAgent(lifecycleOwner) != null) {
        agent = it.getStateAgent(lifecycleOwner)
        return@forEach
      }
    }
    return agent
  }

  override fun injectAgent(instance: Any?) {
    managers.forEach {
      it.injectAgent(instance)
    }
  }

  override fun getStateClass(lifecycleOwner: LifecycleOwner?): Class<*>? {
    var stateClass: Class<*>? = null
    managers.forEach {
      if (it.getStateClass(lifecycleOwner) != null) {
        stateClass = it.getStateClass(lifecycleOwner)
        return@forEach
      }
    }
    return stateClass
  }
}