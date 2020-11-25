package com.landside.submodule

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.BindState
import com.landside.shadowstate_annotation.InjectAgent
import kotlinx.android.synthetic.main.activity_sub.tv_name

@BindState(state = SubState::class, agent = SubStateAgent::class)
class SubActivity : AppCompatActivity() {

  @InjectAgent
  lateinit var agent: SubStateAgent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_sub)
    ShadowState.bind(this)
    ShadowState.injectDispatcher(this,this)
  }

  fun setName(name: String) {
    tv_name.text = name
  }

  fun changeName(view: View) {
    agent.setState { it.copy(subName = it.subName + "++") }
  }
}
