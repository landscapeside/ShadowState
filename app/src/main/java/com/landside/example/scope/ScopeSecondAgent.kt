package com.landside.example.scope

import android.os.Bundle
import com.landside.example.viewpager.TabView
import com.landside.shadowstate.ScopeAgent

class ScopeSecondAgent:ScopeAgent<ScopeInfo, ScopeView>() {
  override fun initState(bundle: Bundle?): ScopeInfo = ScopeInfo(foo = "i am second activity")

  override fun conf() {
    listen({it.foo},{view?.setFoo(it)})
  }
}