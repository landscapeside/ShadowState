package com.landside.example

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.BindAgent
import com.landside.shadowstate_annotation.BindState
import com.landside.submodule.SubActivity
import kotlinx.android.synthetic.main.activity_main.*

@BindState(MainState::class)
@BindAgent(MainAgent::class)
class MainActivity : AppCompatActivity(),MainView {
  lateinit var presenter:TestPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ShadowState.bind(this)
    presenter = TestPresenter()
  }

  override fun setName(name: String){
    tv_name.text = name
  }

  fun changeName(view: View) {
    presenter.changeName()
  }

  fun toSub(view: View) {
    startActivity(Intent(this, SubActivity::class.java))
  }
}
