package com.landside.example.scope

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.landside.example.R
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.InjectAgent
import com.landside.shadowstate_annotation.ScopeState
import kotlinx.android.synthetic.main.activity_scope_test_first.open_second
import kotlinx.android.synthetic.main.activity_scope_test_first.reopen
import kotlinx.android.synthetic.main.activity_scope_test_first.start_change
import kotlinx.android.synthetic.main.activity_scope_test_first.tv_foo
import kotlin.concurrent.thread

@ScopeState(states = [ScopeInfo::class],agents = [ScopeFirstAgent::class])
class ScopeTestFirstActivity: AppCompatActivity(R.layout.activity_scope_test_first),ScopeView {

  @InjectAgent
  lateinit var agent: ScopeFirstAgent

  private var count:Int = 0
  private var thread: Thread? = null
  @Volatile private var counting = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ShadowState.bind(this)

    ShadowState.injectDispatcher(this,this)
    tv_foo.text = agent.state.foo
    reopen.setOnClickListener {
      startActivity(Intent(this,ScopeTestFirstActivity::class.java))
    }
    open_second.setOnClickListener {
      startActivity(Intent(this,ScopeTestSecondActivity::class.java))
    }
    start_change.setOnClickListener {
      thread = thread {
        while (counting) {
          try {
            Thread.sleep(1000)
          } catch (e: InterruptedException) {
          }
          count++
          runOnUiThread {
            agent.setState { it.copy(foo = "i am counting....${count}") }
          }
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    counting = false
    thread?.interrupt()
  }

  override fun setFoo(foo: String) {
    tv_foo.text = foo
  }
}