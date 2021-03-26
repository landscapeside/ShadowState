package com.landside.shadowstate

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import org.jetbrains.annotations.NotNull

abstract class AttachAgent<STATE : Any, VIEW> : ShadowStateAgent<STATE, VIEW>() {

  abstract fun initState(bundle: Bundle?): STATE

  override val state: STATE
    get() = try {
      liveData.value ?: initState(null)
    } catch (e: Exception) {
      initState(null)
    }

  @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
  fun onStop(@NotNull owner: LifecycleOwner) {
    if (owner is Fragment && !owner.isAdded) {
      lifecycleEvents.onNext(Lifecycle.Event.ON_DESTROY)
      ShadowState.detachFragment(owner)
    }
  }
}