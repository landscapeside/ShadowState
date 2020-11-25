package com.landside.example.share

import com.landside.shadowstate.ShadowStateAgent

class MainShareAgent : ShadowStateAgent<Share, ShareView>() {

  override fun conf() {
    listen({ it.shareName }, { view?.setShareName(it) })
    listen({ it.shareCount }, { view?.setShareCount(it) })
    listen({ it.item }, { view?.setShareItem(it) })
  }
}