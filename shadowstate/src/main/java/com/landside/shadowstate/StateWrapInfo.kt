package com.landside.shadowstate

class StateWrapInfo(
  val stateCls: Class<*>,
  val agent: StateAgent<*,*>,
  val binder: StateBinder
)