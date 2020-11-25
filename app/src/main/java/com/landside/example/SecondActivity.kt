package com.landside.example

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.landside.example.share.Share
import com.landside.example.share.Share.ShareItem
import com.landside.example.share.MainShareAgent
import com.landside.example.share.SecondShareAgent
import com.landside.example.share.ShareView
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.BindState
import com.landside.shadowstate_annotation.ShareState
import kotlinx.android.synthetic.main.activity_second.tv_name
import kotlinx.android.synthetic.main.activity_second.tv_share_count
import kotlinx.android.synthetic.main.activity_second.tv_share_item
import kotlinx.android.synthetic.main.activity_second.tv_share_name

@BindState(state = MainState::class, agent = CloneMainAgent::class)
@ShareState(states = [Share::class],agent = [SecondShareAgent::class])
class SecondActivity : AppCompatActivity(),MainView ,ShareView{
  lateinit var presenter: SecondPresenter
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_second)
    ShadowState.bind(this)
    presenter = SecondPresenter(this)
    tv_name.setOnClickListener {
      presenter.changeName()
    }
    setShareName(presenter.shareAgent.state.shareName)
    setShareCount(presenter.shareAgent.state.shareCount)
    setShareItem(presenter.shareAgent.state.item)
  }

  override fun setName(name: String) {
    tv_name.text = name
  }

  override fun setListContents(list: List<String>) {

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
