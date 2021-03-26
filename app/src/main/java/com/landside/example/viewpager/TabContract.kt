package com.landside.example.viewpager

import com.landside.example.share.Share

class TabContract {
    interface MainTabView{
        fun setShareName(name:String)
        fun setShareCount(count:Int)
        fun setShareItem(item: Share.ShareItem<String>)
    }

    interface TabView1{
        fun setShareName(name:String)
        fun setShareCount(count:Int)
        fun setShareItem(item: Share.ShareItem<String>)
    }

    interface TabView2{
        fun setShareName(name:String)
        fun setShareCount(count:Int)
        fun setShareItem(item: Share.ShareItem<String>)
    }

    interface AttachActivityView{
        fun setAttachName(name: String)
    }
}