package com.landside.example.viewpager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.landside.example.R
import com.landside.example.share.MainShareAgent
import com.landside.example.share.Share
import com.landside.example.share.Share.ShareItem
import com.landside.example.share.ShareView
import com.landside.example.viewpager.TabContract.AttachActivityView
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.AttachState
import com.landside.shadowstate_annotation.InjectAgent
import com.landside.shadowstate_annotation.ShareState
import kotlinx.android.synthetic.main.fragment_tab.change_attach
import kotlinx.android.synthetic.main.fragment_tab.tv_attach_name
import kotlinx.android.synthetic.main.fragment_tab.tv_share_count
import kotlinx.android.synthetic.main.fragment_tab.tv_share_item
import kotlinx.android.synthetic.main.fragment_tab.tv_share_name

@ShareState(states = [Share::class],agent = [Tab1ShareAgent::class])
@AttachState(state = AttachInfo::class,agent = FragmentAttachAgent::class)
class TabFragment1:Fragment(),TabContract.MainTabView ,AttachActivityView{

  @InjectAgent
  lateinit var shareAgent: Tab1ShareAgent
  @InjectAgent
  lateinit var attachAgent:FragmentAttachAgent


  override fun onCreate(savedInstanceState: Bundle?) {
    ShadowState.bind(this)
    ShadowState.injectDispatcher(this,this)
    super.onCreate(savedInstanceState)
    Log.d("fragment生命周期","onCreate")
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    Log.d("fragment生命周期","onCreateView")
    return inflater.inflate(R.layout.fragment_tab,container,false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    Log.d("fragment生命周期","onActivityCreated")
    ShadowState.bind(this)
    ShadowState.injectDispatcher(this,this)
    setShareName(shareAgent.state.shareName)
    setShareCount(shareAgent.state.shareCount)
    setShareItem(shareAgent.state.item)
    setAttachName(attachAgent.state.name)
    change_attach.text = "开一个新的tab页面"
    change_attach.setOnClickListener {
      startActivity(Intent(requireContext(), TabActivity::class.java))
    }
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

  override fun setAttachName(name: String) {
    tv_attach_name.text = "当前页面附加状态的名字：${name}"
  }

  override fun onDetach() {
    Log.d("fragment生命周期","onDetach")
    super.onDetach()
  }

  override fun onDestroy() {
    Log.d("fragment生命周期","onDestroy")
    super.onDestroy()
  }

  override fun onPause() {
    Log.d("fragment生命周期","onPause")
    super.onPause()
  }

  override fun onStop() {
    Log.d("fragment生命周期","onStop")
    super.onStop()
  }

  override fun onDestroyView() {
    Log.d("fragment生命周期","onDestroyView")
    super.onDestroyView()
  }


}