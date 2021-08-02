package com.landside.example

data class MainState(
    val name: String = "",
    val amendInternal: Int = 0,
    val childState:ChildState = ChildState(),
    val listStates:List<String> = listOf()
){
    data class ChildState(
        val gender:String = "man"
    )
}