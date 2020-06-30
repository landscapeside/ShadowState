package com.landside.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.BindAgent
import com.landside.shadowstate_annotation.BindState
import kotlinx.android.synthetic.main.activity_main.*

@BindState(TestState::class)
@BindAgent(TestAgent::class)
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
}
