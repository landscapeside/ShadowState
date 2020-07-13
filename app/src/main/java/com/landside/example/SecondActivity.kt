package com.landside.example

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.BindState
import kotlinx.android.synthetic.main.activity_second.tv_name

@BindState(state = MainState::class, agent = CloneMainAgent::class)
class SecondActivity : AppCompatActivity(),MainView {
  lateinit var presenter: SecondPresenter
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_second)
    ShadowState.bind(this)
    presenter = SecondPresenter()
    tv_name.setOnClickListener {
      presenter.changeName()
    }
  }

  override fun setName(name: String) {
    tv_name.text = name
  }

  override fun setListContents(list: List<String>) {

  }

  fun openWatcher(view: View){
    ShadowState.openWatcher()
  }
}
