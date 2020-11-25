package com.landside.example

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.landside.example.share.Share
import com.landside.example.share.Share.ShareItem
import com.landside.example.share.MainShareAgent
import com.landside.example.share.ShareView
import com.landside.example.viewpager.TabActivity
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.BindState
import com.landside.shadowstate_annotation.ShareState
import kotlinx.android.synthetic.main.activity_main.tv_list_contents
import kotlinx.android.synthetic.main.activity_main.tv_name
import kotlinx.android.synthetic.main.activity_main.tv_share_count
import kotlinx.android.synthetic.main.activity_main.tv_share_item
import kotlinx.android.synthetic.main.activity_main.tv_share_name

@BindState(state = MainState::class, agent = MainAgent::class)
@ShareState(states = [Share::class],agent = [MainShareAgent::class])
class MainActivity : AppCompatActivity(), MainView,ShareView {
  lateinit var presenter: MainPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ShadowState.bind(this)
    presenter = MainPresenter(this)
    setListContents(presenter.agent.state.listStates)
  }

  override fun setName(name: String) {
    tv_name.text = name
  }

  override fun setListContents(list: List<String>) {
    tv_list_contents.text = list.joinToString("|")
  }

  fun changeName(view: View) {
    presenter.changeName()
  }

  fun toSub(view: View) {
    startActivity(Intent(this, SecondActivity::class.java))
  }

  fun toViewPager(view: View) {
    startActivity(Intent(this, TabActivity::class.java))
  }

  fun openWatcher(view: View){
    ShadowState.openWatcher()
  }

  fun openShareWatcher(view: View){
    ShadowState.openShareWatcher()
  }

  override fun setShareName(name: String) {
    tv_share_name.text = name
  }

  override fun setShareCount(count: Int) {
    tv_share_count.text = "$count"
  }

  override fun setShareItem(item: ShareItem<String>) {
    tv_share_item.text = "$item"
  }
}
