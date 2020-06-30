package com.landside.shadowstate.example

import android.os.Bundle
import com.landside.shadowstate.StateAgent

class TestAgent : StateAgent<TestState, MainActivity>() {
    override fun initState(bundle: Bundle?): TestState =
        TestState("hahah")

    override fun conf() {
        listen({ it.name }, {
            view?.setName("$it${state.amendInternal}")
            setState { it.copy(amendInternal = it.amendInternal+1) }
        })
    }


}