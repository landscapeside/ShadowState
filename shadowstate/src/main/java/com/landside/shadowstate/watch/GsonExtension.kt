package com.landside.shadowstate.watch

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

fun JsonElement.absorb(newObject: JsonElement) {
  when (this) {
    is JsonObject -> {
      val entrySets = entrySet().toList()
      entrySets.forEach {
        if (it.value.toString() != (newObject as JsonObject)[it.key].toString()) {
          if (it.value is JsonObject || it.value is JsonArray) {
            it.value.absorb(newObject[it.key])
          } else {
            remove(it.key)
            add(it.key, newObject[it.key])
          }
        }
      }
    }
    is JsonArray -> {
      this.forEachIndexed { index, jsonElement ->
        if (jsonElement.toString() != (newObject as JsonArray)[index].toString()) {
          if (jsonElement is JsonObject || jsonElement is JsonArray) {
            jsonElement.absorb(newObject[index])
          } else {
            set(index, newObject[index])
          }
        }
      }
    }
  }
}