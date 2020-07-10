package com.landside.example

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.BindState
import kotlinx.android.synthetic.main.activity_main.tv_list_contents
import kotlinx.android.synthetic.main.activity_main.tv_name

@BindState(state = MainState::class, agent = MainAgent::class)
class MainActivity : AppCompatActivity(), MainView {
  lateinit var presenter: MainPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ShadowState.bind(this)
    presenter = MainPresenter()
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

  fun openWatcher(view: View){
    ShadowState.openWatcher()
  }
}
