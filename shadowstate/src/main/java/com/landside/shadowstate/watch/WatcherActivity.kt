package com.landside.shadowstate.watch

import android.app.AlertDialog.Builder
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.landside.shadowstate.JSONS
import com.landside.shadowstate.R
import com.landside.shadowstate.ShadowState
import kotlinx.android.synthetic.main.view_watcher.btn_add_new_item
import kotlinx.android.synthetic.main.view_watcher.btn_back
import kotlinx.android.synthetic.main.view_watcher.btn_save
import kotlinx.android.synthetic.main.view_watcher.state_members
import kotlinx.android.synthetic.main.view_watcher.tv_current_path
import kotlinx.android.synthetic.main.view_watcher.tv_state_name

class WatcherActivity : AppCompatActivity() {
  var currentPath: String = "."
  val wholeData = JSONS.parseJsonObject(ShadowState.getCurrentStateJsonString())
  val visitStack: MutableList<JsonElement?> = mutableListOf()
  lateinit var adapter: WatcherAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.view_watcher)
    visitStack.add(wholeData)
    tv_current_path.text = "$currentPath"
    tv_state_name.text = ShadowState.getCurrentStateName()
    btn_back.setOnClickListener {
      currentPath = currentPath.substringBeforeLast("/")
      if (currentPath == ".") {
        tv_current_path.text = "$currentPath/"
      } else {
        tv_current_path.text = currentPath
      }
      if (visitStack.size > 1) {
        visitStack.removeAt(visitStack.size - 1)
        adapter.data = visitStack.last() ?: throw IllegalStateException("")
        adapter.notifyDataSetChanged()
        if (adapter.data is JsonObject){
          btn_add_new_item.visibility = View.GONE
        }else{
          btn_add_new_item.visibility = View.VISIBLE
        }
      }
    }
    btn_add_new_item.setOnClickListener {
      (adapter.data as JsonArray).add("")
      adapter.notifyDataSetChanged()
    }
    adapter = WatcherAdapter(
        this,
        wholeData ?: throw IllegalStateException(""), "",
        { name, position, oldValue ->
          val editText = EditText(this)
          editText.setText(oldValue)
          Builder(this).setTitle("修改键值")
              .setView(editText)
              .setPositiveButton("修改输入框内容") { _, _ ->
                if (adapter.data is JsonObject) {
                  (adapter.data as JsonObject).remove(name)
                  (adapter.data as JsonObject).addProperty(name, editText.text.toString())
                } else {
                  (adapter.data as JsonArray).set(position, JsonPrimitive(editText.text.toString()))
                }
                adapter.notifyDataSetChanged()
              }
              .create()
              .show()
        },
        { subArray, name ->
          visitStack.add(subArray)
          setPath(name)
          adapter.data = subArray
          adapter.arrayName = name
          adapter.notifyDataSetChanged()
          btn_add_new_item.visibility = View.VISIBLE
        },
        { subObject, name ->
          visitStack.add(subObject)
          setPath(name)
          adapter.data = subObject
          adapter.notifyDataSetChanged()
          btn_add_new_item.visibility = View.GONE
        },
        { position ->
          runOnUiThread {
            (adapter.data as JsonArray).remove(position)
            adapter.notifyDataSetChanged()
          }
        })
    state_members.layoutManager = LinearLayoutManager(this)
    state_members.adapter = adapter
    btn_save.setOnClickListener {
      ShadowState.reloadState(wholeData.toString())
      finish()
    }
  }

  private fun setPath(path: String) {
    currentPath += "/$path"
    tv_current_path.text = currentPath
  }
}
