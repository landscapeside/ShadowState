package com.landside.shadowstate

import com.landside.shadowstate_annotation.ShareState

object AnnotationHelper {
//  fun getShareStateCls(instance: Any): Class<*>? {
//    if (instance.javaClass.isAnnotationPresent(ShareState::class.java)) {
//      return instance.javaClass.getAnnotation(ShareState::class.java)
//          ?.states?.java
//    }
//    return null
//  }
//
//  fun getShareAgentCls(instance: Any):Class<*>?{
//    if (instance.javaClass.isAnnotationPresent(ShareState::class.java)) {
//      return instance.javaClass.getAnnotation(ShareState::class.java)
//          ?.agent?.java
//    }
//    return null
//  }

  fun isAnnotationPresent(instance: Any,clazz:Class<out Annotation>):Boolean = instance.javaClass.isAnnotationPresent(clazz)
}