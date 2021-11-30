package com.landside.example.scope

import android.os.Bundle
import com.landside.example.viewpager.TabView
import com.landside.shadowstate.ScopeAgent

class ScopeFirstAgent:ScopeAgent<ScopeInfo, ScopeView>() {
  override fun initState(bundle: Bundle?): ScopeInfo = ScopeInfo(foo = "i am first activity")

  override fun conf() {
    listen({it.foo},{view?.setFoo(it)})
  }
}