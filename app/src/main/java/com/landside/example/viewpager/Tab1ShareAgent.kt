package com.landside.example.viewpager

import com.landside.example.share.Share
import com.landside.shadowstate.ShadowStateAgent

class Tab1ShareAgent:ShadowStateAgent<Share, TabContract.MainTabView>() {
    override fun conf() {
        listen({it.shareName},{
            view?.setShareName(it)
        })
        listen({it.shareCount},{view?.setShareCount(it)})
        listen({it.item},{view?.setShareItem(it)})
    }
}