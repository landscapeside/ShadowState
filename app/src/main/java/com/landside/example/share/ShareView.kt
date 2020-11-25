package com.landside.example.share

import com.landside.example.share.Share.ShareItem

interface ShareView {
  fun setShareName(name:String)
  fun setShareCount(count:Int)
  fun setShareItem(item:ShareItem<String>)
}