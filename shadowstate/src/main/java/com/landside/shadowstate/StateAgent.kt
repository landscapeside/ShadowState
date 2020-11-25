package com.landside.shadowstate

import android.os.Bundle

abstract class StateAgent<STATE : Any, VIEW> : ShadowStateAgent<STATE, VIEW>() {

  abstract fun initState(bundle: Bundle?): STATE

  override val state: STATE
    get() = try {
      liveData.value ?: initState(null)
    } catch (e: Exception) {
      initState(null)
    }
}