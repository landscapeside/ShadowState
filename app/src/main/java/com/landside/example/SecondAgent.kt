package com.landside.example

import android.os.Bundle
import com.landside.shadowstate.StateAgent

class SecondAgent:StateAgent<SecondState,SecondActivity>() {
    override fun initState(bundle: Bundle?): SecondState =
        SecondState()

    override fun conf() {
        listen({it.name},{
            view?.setName(it)
        })
    }
}