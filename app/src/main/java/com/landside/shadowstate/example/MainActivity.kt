package com.landside.shadowstate.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.landside.shadowstate.R
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.BindAgent
import com.landside.shadowstate_annotation.BindState
import com.landside.shadowstate_annotation.InjectAgent
import kotlinx.android.synthetic.main.activity_main.*

@BindState(TestState::class)
@BindAgent(TestAgent::class)
class MainActivity : AppCompatActivity() {

  @InjectAgent
  lateinit var agent: TestAgent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ShadowState.bind(this)
    ShadowState.injectDispatcher(this)
    setName(agent.state.name)
  }

  fun setName(name: String){
    tv_name.text = name
  }

  fun changeName(view: View) {
    agent.setState { it.copy(name = it.name+"++") }
  }
}
