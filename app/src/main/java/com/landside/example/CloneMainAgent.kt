package com.landside.example

import android.os.Bundle
import com.landside.shadowstate.StateAgent

class CloneMainAgent : StateAgent<MainState, MainView>() {
    override fun initState(bundle: Bundle?): MainState =
        MainState(name = "hahah",listStates = listOf("1","2"))

    override fun conf() {
        listen({ it.name }, {
            view?.setName("$it${state.amendInternal}  gender:${state.childState.gender}")
            setState { it.copy(amendInternal = it.amendInternal+1) }
        })
        listen({it.listStates},{
            view?.setListContents(it)
        })
    }
}