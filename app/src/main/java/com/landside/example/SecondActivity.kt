package com.landside.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.BindAgent
import com.landside.shadowstate_annotation.BindState
import kotlinx.android.synthetic.main.activity_second.*

@BindState(SecondState::class)
@BindAgent(SecondAgent::class)
class SecondActivity : AppCompatActivity() {
    lateinit var presenter:SecondPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        ShadowState.bind(this)
        presenter = SecondPresenter()
        tv_name.setOnClickListener {
            presenter.changeName()
        }
    }

    fun setName(name: String){
        tv_name.text = name
    }
}
