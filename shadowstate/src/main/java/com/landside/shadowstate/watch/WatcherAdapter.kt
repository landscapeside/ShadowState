package com.landside.shadowstate.watch

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.landside.shadowstate.R
import kotlinx.android.synthetic.main.item_array_state_watcher.view.iv_array_item_del
import kotlinx.android.synthetic.main.item_array_state_watcher.view.tv_array_value
import kotlinx.android.synthetic.main.item_object_state_watcher.view.tv_name
import kotlinx.android.synthetic.main.item_object_state_watcher.view.tv_value

class WatcherAdapter(
  val context: Context,
  var data: JsonElement,
  var arrayName: String = "",
  val amendValue: (String, Int, String) -> Unit,
  val toArray: (JsonElement, String) -> Unit,
  val toObject: (JsonElement, String) -> Unit,
  val delArrayItem: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  val inflater: LayoutInflater = LayoutInflater.from(context)

  class ObjectHolder(val container: View) : RecyclerView.ViewHolder(container)
  class ArrayHolder(val container: View) : RecyclerView.ViewHolder(container)

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): RecyclerView.ViewHolder = if (viewType == TYPE_OBJECT) {
    ObjectHolder(inflater.inflate(R.layout.item_object_state_watcher, parent, false))
  } else {
    ArrayHolder(inflater.inflate(R.layout.item_array_state_watcher, parent, false))
  }

  override fun getItemViewType(position: Int): Int =
    if (data is JsonObject) TYPE_OBJECT else TYPE_ARRAY

  override fun getItemCount(): Int =
    if (data is JsonObject) (data as JsonObject).entrySet().size
    else (data as JsonArray).size()

  override fun onBindViewHolder(
    holder: RecyclerView.ViewHolder,
    position: Int
  ) {
    if (holder is ObjectHolder) {
      val element = (data as JsonObject).entrySet().toList()[position]
      if (element.value.isJsonArray) {
        holder.itemView.tv_value.text = "[数组]"
        holder.itemView.tv_value.setOnClickListener {
          toArray(element.value, element.key)
        }
      } else if (element.value.isJsonObject) {
        holder.itemView.tv_value.text = "[对象]"
        holder.itemView.tv_value.setOnClickListener {
          toObject(element.value, element.key)
        }
      } else if (element.value.isJsonPrimitive) {
        holder.itemView.tv_value.text = element.value.asJsonPrimitive.asString
        holder.itemView.tv_value.setOnClickListener {
          amendValue(element.key, position, holder.itemView.tv_value.text.toString())
        }
      } else {
        holder.itemView.tv_value.text = "[null]"
        holder.itemView.tv_value.setOnClickListener {
          amendValue(element.key, position, "")
        }
      }
      holder.itemView.tv_name.text = element.key
    } else {
      val element = (data as JsonArray)[position]
      if (element.isJsonArray) {
        holder.itemView.tv_array_value.text = "[数组]"
        holder.itemView.tv_array_value.setOnClickListener {
          toArray(element, "$arrayName[$position]")
        }
      } else if (element.isJsonObject) {
        holder.itemView.tv_array_value.text = "[对象]"
        holder.itemView.tv_array_value.setOnClickListener {
          toObject(element, "$arrayName[$position]")
        }
      } else if (element.isJsonPrimitive) {
        holder.itemView.tv_array_value.text = element.asJsonPrimitive.asString
        holder.itemView.tv_array_value.setOnClickListener {
          amendValue("", position, holder.itemView.tv_array_value.text.toString())
        }
      } else {
        holder.itemView.tv_array_value.text = "[null]"
        holder.itemView.tv_array_value.setOnClickListener {
          amendValue("", position, "")
        }
      }
      holder.itemView.iv_array_item_del.setOnClickListener {
          delArrayItem(position)
      }
    }

  }

  companion object {
    const val TYPE_OBJECT = 1
    const val TYPE_ARRAY = 2
  }
}