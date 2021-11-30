package com.landside.example

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.FileUtils
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.random.Random

fun Uri?.toRealPath(context: Context): String =
  if (this == null) "" else AlbumHelper.getRealPathFromUri(context, this) ?: ""

object AlbumHelper {

  @SuppressLint("NewApi") fun getRealPathFromUri(
    context: Context,
    uri: Uri
  ): String? {
    val sdkVersion: Int = Build.VERSION.SDK_INT
    return when {
      sdkVersion >= Build.VERSION_CODES.Q ->
        getRealPathFromUriAboveApi29(context, uri)
      sdkVersion >= Build.VERSION_CODES.N && sdkVersion < Build.VERSION_CODES.Q ->
        getRealPathFromUriAboveApi24(context, uri)
      sdkVersion >= Build.VERSION_CODES.KITKAT && sdkVersion < Build.VERSION_CODES.N ->
        getRealPathFromUriAboveApi19(context, uri)
      else -> getRealPathFromUriBelowAPI19(context, uri)
    }
  }

  /**
   * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
   *
   * @param context 上下文对象
   * @param uri     图片的Uri
   * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
   */
  private fun getRealPathFromUriBelowAPI19(
    context: Context,
    uri: Uri
  ): String? {
    return getDataColumn(
        context, uri, null, null
    )
  }

  /**
   * 适配api19及以上,根据uri获取图片的绝对路径
   *
   * @param context 上下文对象
   * @param uri     图片的Uri
   * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
   */
  @SuppressLint("NewApi")
  private fun getRealPathFromUriAboveApi19(
    context: Context,
    uri: Uri
  ): String? {
    var filePath: String? = null
    if (DocumentsContract.isDocumentUri(
            context,
            uri
        )
    ) { // 如果是document类型的 uri, 则通过document id来进行处理
      val documentId = DocumentsContract.getDocumentId(uri)
      if (isMediaDocument(uri)) { // 使用':'分割
        val id = documentId.split(":")
            .toTypedArray()[1]
        val selection = MediaStore.Images.Media._ID + "=?"
        val selectionArgs = arrayOf(id)
        filePath = getDataColumn(
            context,
            Media.EXTERNAL_CONTENT_URI,
            selection,
            selectionArgs
        )
      } else if (isDownloadsDocument(
              uri
          )
      ) {
        val contentUri: Uri = ContentUris.withAppendedId(
            Uri.parse("content://downloads/public_downloads"),
            java.lang.Long.valueOf(documentId)
        )
        filePath = getDataColumn(
            context, contentUri, null, null
        )
      }
    } else if ("content".equals(uri.scheme, ignoreCase = true)) { // 如果是 content 类型的 Uri
      filePath = getDataColumn(
          context, uri, null, null
      )
    } else if ("file" == uri.scheme) { // 如果是 file 类型的 Uri,直接获取图片对应的路径
      filePath = uri.path
    }
    return filePath
  }

  @RequiresApi(Build.VERSION_CODES.N)
  private fun getRealPathFromUriAboveApi24(
    context: Context,
    uri: Uri
  ): String? {
    try {
      val returnCursor = context.contentResolver
          .query(uri, null, null, null, null)
      val nameIndex =
        returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
      returnCursor.moveToFirst()
      val name = returnCursor.getString(nameIndex)
      val file = File(context.filesDir, name)
      val inputStream: InputStream? = context.contentResolver
          .openInputStream(uri)
      val outputStream = FileOutputStream(file)
      var read = 0
      val maxBufferSize = 1 * 1024 * 1024
      val bytesAvailable: Int = inputStream?.available() ?: 0
      val bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
      val buffers = ByteArray(bufferSize)
      inputStream?.apply {
        while (inputStream.read(buffers)
                .also { read = it } != -1
        ) {
          outputStream.write(buffers, 0, read)
        }
      }
      returnCursor.close()
      inputStream?.close()
      outputStream.close()
      return file.path
    } catch (e: java.lang.Exception) {
      e.printStackTrace()
    }
    return null
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  private fun getRealPathFromUriAboveApi29(
    context: Context,
    uri: Uri
  ): String? =
    if (uri.scheme == ContentResolver.SCHEME_FILE)
      File(requireNotNull(uri.path)).absolutePath
    else if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
      //把文件保存到沙盒
      val contentResolver = context.contentResolver
      val displayName = run {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.let {
          if (it.moveToFirst())
            it.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
          else null
        }
      } ?: "${System.currentTimeMillis()}${Random.nextInt(0, 9999)}.${MimeTypeMap.getSingleton()
          .getExtensionFromMimeType(contentResolver.getType(uri))}"
      val ios = contentResolver.openInputStream(uri)
      if (ios != null) {
        File("${context.externalCacheDir!!.absolutePath}/$displayName")
            .apply {
              val fos = FileOutputStream(this)
              FileUtils.copy(ios, fos)
              fos.close()
              ios.close()
            }.absolutePath
      } else null
    } else null

  /**
   * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
   *
   */
  private fun getDataColumn(
    context: Context,
    uri: Uri,
    selection: String?,
    selectionArgs: Array<String>?
  ): String? {
    var path: String? = null
    val projection =
      arrayOf(MediaStore.Images.Media.DATA)
    var cursor: Cursor? = null
    try {
      cursor =
        context.contentResolver
            .query(uri, projection, selection, selectionArgs, null)
      if (cursor != null && cursor.moveToFirst()) {
        val columnIndex: Int = cursor.getColumnIndexOrThrow(projection[0])
        path = cursor.getString(columnIndex)
      }
    } catch (e: Exception) {
      cursor?.close()
    }
    return path
  }

  /**
   * @param uri the Uri to check
   * @return Whether the Uri authority is MediaProvider
   */
  private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
  }

  /**
   * @param uri the Uri to check
   * @return Whether the Uri authority is DownloadsProvider
   */
  private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
  }
}