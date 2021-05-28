package com.landside.shadowstate.watch

import android.app.Activity
import android.app.AlertDialog.Builder
import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.landside.shadowstate.JSONS
import com.landside.shadowstate.R
import com.landside.shadowstate.ShadowState
import io.reactivex.disposables.Disposable

internal class WatcherHandler(
  val rootView: View
) {
  private val uiHandler = UiHandler(ShadowState.context)
  var currentPath: String = "."
  var wholeData = JSONS.parseJsonObject(ShadowState.getCurrentStateJsonString())
  val visitStack: MutableList<JsonElement?> = mutableListOf()
  private lateinit var adapter: WatcherAdapter
  var tv_current_path: TextView
  lateinit var tv_state_name:TextView
  var btn_add_new_item: View
  lateinit var disposable:Disposable

  init {
    visitStack.add(wholeData)
    disposable = ShadowState.watchObjectPublisher.subscribe {
      if (tv_state_name.text == ShadowState.getCurrentStateName()) {
        wholeData?.absorb(JSONS.parseJsonObject(it) ?: throw java.lang.IllegalStateException())
        adapter.notifyDataSetChanged()
      }
    }
    tv_current_path = rootView.findViewById(R.id.tv_current_path)
    tv_state_name = rootView.findViewById(R.id.tv_state_name)
    tv_state_name.text = ShadowState.getCurrentStateName()
    btn_add_new_item = rootView.findViewById(R.id.btn_add_new_item)
    rootView.findViewById<View>(R.id.btn_back)
        .setOnClickListener {
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
            if (adapter.data is JsonObject) {
              btn_add_new_item.visibility = View.GONE
            } else {
              btn_add_new_item.visibility = View.VISIBLE
            }
          }
        }
    btn_add_new_item.setOnClickListener {
      (adapter.data as JsonArray).add("")
      adapter.notifyDataSetChanged()
    }
    adapter = WatcherAdapter(
        ShadowState.context,
        wholeData ?: throw IllegalStateException(""), "",
        { name, position, oldValue ->
          val editText = EditText(ShadowState.context)
          editText.setText(oldValue)
          try {
            Builder(if (ShadowState.pagesStack.last() is Activity) ShadowState.pagesStack.last() as Activity else (ShadowState.pagesStack.last() as Fragment).activity!!)
                .setTitle("修改键值")
                .setView(editText)
                .setPositiveButton("修改输入框内容") { _, _ ->
                  if (adapter.data is JsonObject) {
                    (adapter.data as JsonObject).remove(name)
                    (adapter.data as JsonObject).addProperty(name, editText.text.toString())
                  } else {
                    (adapter.data as JsonArray).set(
                        position, JsonPrimitive(editText.text.toString())
                    )
                  }
                  adapter.notifyDataSetChanged()
                }
                .create()
                .show()
          } catch (e: Exception) {
            e.printStackTrace()
          }
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
          uiHandler.post {
            (adapter.data as JsonArray).remove(position)
            adapter.notifyDataSetChanged()
          }
        })
    rootView.findViewById<RecyclerView>(R.id.state_members)
        .layoutManager = LinearLayoutManager(ShadowState.context)
    rootView.findViewById<RecyclerView>(R.id.state_members)
        .adapter = adapter
    rootView.findViewById<View>(R.id.btn_save)
        .setOnClickListener {
          ShadowState.reloadState(wholeData.toString())
        }
  }

  private fun setPath(path: String) {
    currentPath += "/$path"
    tv_current_path.text = currentPath
  }

  fun dispose(){
    disposable.dispose()
  }
}