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

  override fun getAgentClass(lifecycleOwner: LifecycleOwner?): Class<*>? {
    var agentCls: Class<*>? = null
    managers.forEach {
      if (it.getAgentClass(lifecycleOwner) != null) {
        agentCls = it.getAgentClass(lifecycleOwner)
        return@forEach
      }
    }
    return agentCls
  }

  override fun injectAgent(instance: Any?,owner: LifecycleOwner?) {
    managers.forEach {
      it.injectAgent(instance,owner)
    }
  }

  override fun rebind(lifecycleOwner: LifecycleOwner?) {
    managers.forEach {
      it.rebind(lifecycleOwner)
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