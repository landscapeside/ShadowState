package com.landside.example

import android.os.Bundle
import com.landside.shadowstate.StateAgent

class MainAgent : StateAgent<MainState, MainView>() {
    override fun initState(bundle: Bundle?): MainState =
        MainState("hahah")

    override fun conf() {
        listen({ it.name }, {
            view?.setName("$it${state.amendInternal}")
            setState { it.copy(amendInternal = it.amendInternal+1) }
        })
    }
}