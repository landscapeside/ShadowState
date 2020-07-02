package com.landside.shadowstate

import androidx.lifecycle.LifecycleOwner
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import java.lang.IllegalStateException

object ShadowState {

    fun init(loggable:Boolean) {
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
        if (ZipStateManager.managers.isEmpty()){
            throw IllegalStateException("There is no stateManager!You need to add a class that is annotated by @StateManagerProvider to your module! ")
        }
        ZipStateManager.bind(lifecycleOwner)
    }

    var stateRecord: (Any) -> Unit = {
        Logger.w(it.javaClass.canonicalName ?: "")
        Logger.json(JSONS.parseJson(it))
    }

    fun injectDispatcher(instance:Any){
        if (ZipStateManager.managers.isEmpty()){
            throw IllegalStateException("There is no stateManager!You need to add a class that is annotated by @StateManagerProvider to your module!")
        }
        ZipStateManager.injectAgent(instance)
    }
}