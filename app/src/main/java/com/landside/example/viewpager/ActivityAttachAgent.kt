package com.landside.example.viewpager

import android.os.Bundle
import com.landside.example.viewpager.TabContract.AttachActivityView
import com.landside.shadowstate.AttachAgent

class ActivityAttachAgent : AttachAgent<AttachInfo, AttachActivityView>() {
  override fun initState(bundle: Bundle?): AttachInfo = AttachInfo("初始名字")

  override fun conf() {
    listen({ it.name }, {
      view?.setAttachName(it)
    })
  }
}