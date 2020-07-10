package com.landside.shadowstate.watch

import android.content.Context
import android.content.ContextWrapper
import android.view.ContextThemeWrapper
import android.view.View
import android.view.WindowManager.LayoutParams
import com.landside.shadowstate.R
import com.stardust.enhancedfloaty.FloatyService
import com.stardust.enhancedfloaty.ResizableExpandableFloaty.AbstractResizableExpandableFloaty
import com.stardust.enhancedfloaty.ResizableExpandableFloatyWindow

class WatcherFloaty : AbstractResizableExpandableFloaty() {

  init {
    setShouldRequestFocusWhenExpand(false)
    initialX = 0
    initialY = 500
    collapsedViewUnpressedAlpha = 1.0f
  }

  override fun getInitialWidth(): Int {
    return LayoutParams.WRAP_CONTENT
  }

  override fun getInitialHeight(): Int {
    return LayoutParams.WRAP_CONTENT
  }

  private var mResizer: View? = null
  private var mMoveCursor: View? = null

  override fun getResizerView(expandedView: View): View? {
    mResizer = expandedView.findViewById(R.id.resizer)
    return mResizer
  }

  override fun getMoveCursorView(expandedView: View): View? {
    mMoveCursor = expandedView.findViewById(R.id.move_cursor)
    return mMoveCursor
  }

  private var mContextWrapper: ContextWrapper? = null

  private fun ensureContextWrapper(context: Context) {
    if (mContextWrapper == null) {
      mContextWrapper = ContextThemeWrapper(context, R.style.ConsoleTheme)
    }
  }

  override fun inflateExpandedView(
    service: FloatyService?,
    window: ResizableExpandableFloatyWindow?
  ): View {
    ensureContextWrapper(service as Context)
    val view =
      View.inflate(mContextWrapper, R.layout.floating_console_expand, null)
    setWindowOperationIconListeners(view, window!!)
    initWatcher(view)
//    setInitialMeasure(view)
    return view
  }

  private fun setWindowOperationIconListeners(
    view: View,
    window: ResizableExpandableFloatyWindow
  ) {
    view.findViewById<View>(R.id.close)
        .setOnClickListener { v: View? ->
          window.close()
          try {
            watcherHandlers.last()
                .dispose()
          } catch (e: Exception) {
          }
        }
    view.findViewById<View>(R.id.move_or_resize)
        .setOnClickListener { v: View? ->
          if (mMoveCursor!!.visibility == View.VISIBLE) {
            mMoveCursor!!.visibility = View.GONE
            mResizer!!.visibility = View.GONE
          } else {
            mMoveCursor!!.visibility = View.VISIBLE
            mResizer!!.visibility = View.VISIBLE
          }
        }
    view.findViewById<View>(R.id.minimize)
        .setOnClickListener { v: View? -> window.collapse() }
  }

  val watcherHandlers: MutableList<WatcherHandler> = mutableListOf()

  private fun initWatcher(view: View) {
    watcherHandlers.add(WatcherHandler(view.findViewById<View>(R.id.watcher_box)))
  }

  private fun setInitialMeasure(view: View) {
    view.post {
      ViewUtil.setViewMeasure(
          view, ScreenMetrics.getDeviceScreenWidth() * 2 / 3,
          ScreenMetrics.getDeviceScreenHeight() / 3
      )
    }
  }

  override fun inflateCollapsedView(
    service: FloatyService?,
    p1: ResizableExpandableFloatyWindow?
  ): View {
    ensureContextWrapper(service as Context)
    return View.inflate(mContextWrapper, R.layout.floating_window_collapse, null)
  }
}