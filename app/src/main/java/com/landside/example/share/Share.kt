package com.landside.example.share

data class Share(
  val shareName: String = "",
  val shareCount: Int = 0,
  val item: ShareItem<String> = ShareItem("init")
) {
  data class ShareItem<T>(
    val data: T
  )
}