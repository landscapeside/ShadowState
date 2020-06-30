package com.landside.shadowstate

import androidx.lifecycle.LifecycleOwner
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy

object ShadowState {

    private var stateManager: StateManager? = null
    fun install(stateManager: StateManager,loggable:Boolean) {
        this.stateManager = stateManager
        Logger.addLogAdapter(object : AndroidLogAdapter(
            PrettyFormatStrategy
                .newBuilder()
                .methodCount(0)
                .tag("PAGE_STATE")
                .build()
        ) {
            override fun isLoggable(
                priority: Int,
                tag: String?
            ): Boolean = loggable
        })
    }

    fun bind(lifecycleOwner: LifecycleOwner) {
        stateManager?.bind(lifecycleOwner)
    }

    var stateRecord: (Any) -> Unit = {
        Logger.w(it.javaClass.canonicalName ?: "")
        Logger.json(JSONS.parseJson(it))
    }

    fun injectDispatcher(instance:Any){
        stateManager?.injectAgent(instance)
    }
}