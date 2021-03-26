package com.landside.example.viewpager

import android.os.Bundle
import com.landside.example.viewpager.TabContract.AttachActivityView
import com.landside.shadowstate.AttachAgent
import com.landside.shadowstate.StateAgent

class FragmentAttachAgent : AttachAgent<AttachInfo, AttachActivityView>() {
  override fun initState(bundle: Bundle?): AttachInfo = AttachInfo()

  override fun conf() {
    listen({it.name},{
      view?.setAttachName(it)
    })
  }
}