package com.landside.example.viewpager

import android.content.Intent
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
import com.landside.example.viewpager.TabContract.AttachActivityView
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.AttachState
import com.landside.shadowstate_annotation.InjectAgent
import com.landside.shadowstate_annotation.ShareState
import kotlinx.android.synthetic.main.fragment_tab.change_attach
import kotlinx.android.synthetic.main.fragment_tab.open
import kotlinx.android.synthetic.main.fragment_tab.tv_attach_name
import kotlinx.android.synthetic.main.fragment_tab.tv_share_count
import kotlinx.android.synthetic.main.fragment_tab.tv_share_item
import kotlinx.android.synthetic.main.fragment_tab.tv_share_name

@ShareState(states = [Share::class],agent = [Tab2ShareAgent::class])
@AttachState(state = AttachInfo::class,agent = FragmentAttachAgent::class)
class TabFragment2:Fragment(),TabContract.MainTabView , AttachActivityView {

  lateinit var presenter: TabFragmentPresenter

  @InjectAgent
  lateinit var shareAgent: Tab2ShareAgent

  override fun onCreate(savedInstanceState: Bundle?) {
    ShadowState.bind(this)
    ShadowState.injectDispatcher(this,this)
    super.onCreate(savedInstanceState)
    presenter = TabFragmentPresenter(this)
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
    open.setOnClickListener {
      startActivity(Intent(requireContext(), TabActivity::class.java))
    }
    change_attach.setOnClickListener {
      presenter.change()
    }
  }

  override fun setShareName(name: String) {
    tv_share_name.text = "tab2:$name"
  }

  override fun setShareCount(count: Int) {
    tv_share_count.text = "tab2:$count"
  }

  override fun setShareItem(item: ShareItem<String>) {
    tv_share_item.text = "tab2:${item}"
  }

  override fun setAttachName(name: String) {
    tv_attach_name.text = "当前页面附加状态的名字：${name}"
  }
}