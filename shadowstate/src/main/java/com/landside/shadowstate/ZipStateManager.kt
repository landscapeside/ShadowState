package com.landside.shadowstate

import androidx.lifecycle.LifecycleOwner

object ZipStateManager : StateManager {

    internal val managers = mutableListOf<StateManager>()

    fun zip(vararg managers: StateManager) {
        this.managers.addAll(managers)
    }

    override fun bind(lifecycleOwner: LifecycleOwner?) {
        managers.forEach {
            it.bind(lifecycleOwner)
        }
    }

    override fun injectAgent(instance: Any?) {
        managers.forEach {
            it.injectAgent(instance)
        }
    }
}