package com.landside.example.scope

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.landside.example.R
import kotlinx.android.synthetic.main.activity_scope_test_startup.open

class ScopeTestStartupActivity: AppCompatActivity(R.layout.activity_scope_test_startup) {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    open.setOnClickListener {
      startActivity(Intent(this,ScopeTestFirstActivity::class.java))
    }
  }
}