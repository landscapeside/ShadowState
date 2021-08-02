package com.landside.example.viewpager

import android.os.Bundle
import com.landside.example.viewpager.TabContract.AttachActivityView
import com.landside.shadowstate.AttachAgent
import com.landside.shadowstate.StateAgent

class FragmentAttachAgent2 : AttachAgent<AttachInfo2, AttachActivityView>() {
  override fun initState(bundle: Bundle?): AttachInfo2 = AttachInfo2()

  override fun conf() {
    listen({it.age},{
      view?.setAttachAge(it)
    })
  }
}