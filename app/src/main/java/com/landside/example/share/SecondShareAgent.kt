package com.landside.example.share

import com.landside.shadowstate.ShadowStateAgent

class SecondShareAgent : ShadowStateAgent<Share, ShareView>() {

  override fun conf() {
    listen({ it.shareName }, { view?.setShareName("second Page:$it") })
    listen({ it.shareCount }, { view?.setShareCount(it) })
    listen({ it.item }, { view?.setShareItem(it) })
  }
}