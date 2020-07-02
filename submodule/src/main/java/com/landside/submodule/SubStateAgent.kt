package com.landside.submodule

import android.os.Bundle
import com.landside.shadowstate.StateAgent

class SubStateAgent:StateAgent<SubState,SubActivity>() {
    override fun initState(bundle: Bundle?): SubState  = SubState()

    override fun conf() {
        listen({it.subName},{
            view?.setName(it)
        })
    }
}