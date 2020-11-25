package com.landside.example.viewpager

import android.os.Bundle
import com.landside.shadowstate.StateAgent

class TabAgent:StateAgent<Tab,TabView>() {
  override fun initState(bundle: Bundle?): Tab =
    Tab()

  override fun conf() {

  }
}