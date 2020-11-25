package com.landside.example.viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.landside.example.R
import com.landside.example.share.MainShareAgent
import com.landside.example.share.Share
import com.landside.example.share.Share.ShareItem
import com.landside.example.share.ShareView
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.InjectAgent
import com.landside.shadowstate_annotation.ShareState
import kotlinx.android.synthetic.main.fragment_tab.tv_share_count
import kotlinx.android.synthetic.main.fragment_tab.tv_share_item
import kotlinx.android.synthetic.main.fragment_tab.tv_share_name

@ShareState(states = [Share::class],agent = [MainShareAgent::class])
class TabFragment1:Fragment(),ShareView {

  @InjectAgent
  lateinit var shareAgent: MainShareAgent

  override fun onCreate(savedInstanceState: Bundle?) {
    ShadowState.bind(this)
    ShadowState.injectDispatcher(this,this)
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_tab,container,false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    ShadowState.bind(this)
    ShadowState.injectDispatcher(this,this)
    setShareName(shareAgent.state.shareName)
    setShareCount(shareAgent.state.shareCount)
    setShareItem(shareAgent.state.item)
  }

  override fun setShareName(name: String) {
    tv_share_name.text = "tab1:$name"
  }

  override fun setShareCount(count: Int) {
    tv_share_count.text = "tab1:$count"
  }

  override fun setShareItem(item: ShareItem<String>) {
    tv_share_item.text = "tab1:${item}"
  }
}