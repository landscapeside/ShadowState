package com.landside.shadowstate

import androidx.lifecycle.LifecycleOwner

object ZipStateManager : StateManager {

  internal val managers = mutableListOf<StateManager>()

  fun zip(vararg managers: StateManager) {
    this.managers.addAll(managers)
  }

  override fun getBinder(lifecycleOwner: LifecycleOwner?): StateBinder? {
    var stateBinder: StateBinder? = null
    managers.forEach {
      if (it.getBinder(lifecycleOwner) != null) {
        stateBinder = it.getBinder(lifecycleOwner)
        return@forEach
      }
    }
    return stateBinder
  }

  override fun bind(lifecycleOwner: LifecycleOwner?) {
    managers.forEach {
      it.bind(lifecycleOwner)
    }
  }

  override fun detach(lifecycleOwner: LifecycleOwner?) {
    managers.forEach {
      it.detach(lifecycleOwner)
    }
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

  override fun remove(lifecycleOwner: LifecycleOwner?) {
    managers.forEach {
      it.remove(lifecycleOwner)
    }
  }
}