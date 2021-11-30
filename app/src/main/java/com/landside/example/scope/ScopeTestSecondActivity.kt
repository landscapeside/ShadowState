package com.landside.example.scope

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.landside.example.R
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.InjectAgent
import com.landside.shadowstate_annotation.ScopeState
import kotlinx.android.synthetic.main.activity_scope_test_second.tv_foo

@ScopeState(states = [ScopeInfo::class],agents = [ScopeSecondAgent::class])
class ScopeTestSecondActivity: AppCompatActivity(R.layout.activity_scope_test_second),ScopeView {

  @InjectAgent
  lateinit var agent: ScopeSecondAgent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ShadowState.bind(this)

    ShadowState.injectDispatcher(this,this)
    tv_foo.text = agent.state.foo
  }

  override fun setFoo(foo: String) {
    tv_foo.text = foo
  }
}