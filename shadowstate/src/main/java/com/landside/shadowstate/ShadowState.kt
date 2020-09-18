package com.landside.shadowstate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.landside.shadowstate.watch.FloatingPermission
import com.landside.shadowstate.watch.WatcherFloaty
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.stardust.enhancedfloaty.FloatyService
import com.stardust.enhancedfloaty.ResizableExpandableFloatyWindow
import com.stardust.enhancedfloaty.util.FloatingWindowPermissionUtil
import io.reactivex.subjects.PublishSubject

object ShadowState {

  var pagesStack: MutableList<LifecycleOwner> = mutableListOf()
  var watchStateIdx: Int = 0
  lateinit var context: Context
  lateinit var floatyWindow: ResizableExpandableFloatyWindow
  internal val watchObjectPublisher: PublishSubject<String> = PublishSubject.create()

  fun init(
    context: Context,
    loggable: Boolean,
    useWatcher: Boolean = false
  ) {
    this.context = context
    Logger.addLogAdapter(object : AndroidLogAdapter(
        PrettyFormatStrategy
            .newBuilder()
            .methodCount(0)
            .tag("PAGE_STATE")
            .build()
    ) {
      override fun isLoggable(
        priority: Int,
        tag: String?
      ): Boolean = loggable
    })
    floatyWindow = object : ResizableExpandableFloatyWindow(WatcherFloaty()) {
      override fun onCreate(
        service: FloatyService?,
        manager: WindowManager?
      ) {
        super.onCreate(service, manager)
        expand()
      }

      override fun onServiceDestroy(service: FloatyService?) {
        super.onServiceDestroy(service)
        (floatyWindow.floaty as WatcherFloaty).watcherHandlers.forEach {
          it.dispose()
        }
      }
    }
    if (useWatcher) {
      FloatingWindowPermissionUtil.goToFloatingWindowPermissionSettingIfNeeded(context)
      if (!FloatingPermission.canDrawOverlays(ShadowState.context)) {
        FloatingPermission.manageDrawOverlays(ShadowState.context)
        Toast.makeText(context, R.string.text_no_floating_window_permission, Toast.LENGTH_LONG)
            .show()
      }
      context.startService(Intent(context, FloatyService::class.java))
    }
  }

  fun bind(lifecycleOwner: LifecycleOwner) {
    if (ZipStateManager.managers.isEmpty()) {
      throw IllegalStateException(
          "There is no stateManager!You need to create a class which is annotated by @StateManagerProvider into your module! "
      )
    }
    ZipStateManager.bind(lifecycleOwner)
    pagesStack.add(lifecycleOwner)
  }

  fun rebind(lifecycleOwner: LifecycleOwner) {
    if (ZipStateManager.managers.isEmpty()) {
      throw IllegalStateException(
          "There is no stateManager!You need to add a class that is annotated by @StateManagerProvider to your module! "
      )
    }
    ZipStateManager.rebind(lifecycleOwner)
    pagesStack.add(lifecycleOwner)
  }

  fun removePage(view: Any) {
    pagesStack.remove(view)
  }

  var stateRecord: (Any) -> Unit = {
    Logger.w(it.javaClass.canonicalName ?: "")
    Logger.json(JSONS.parseJson(it))
  }

  val watchRecord: (Any) -> Unit = {
    watchObjectPublisher.onNext(JSONS.parseJson(it) ?: "")
  }

  fun injectDispatcher(
    instance: Any,
    owner: LifecycleOwner
  ) {
    if (ZipStateManager.managers.isEmpty()) {
      throw IllegalStateException(
          "There is no stateManager!You need to add a class that is annotated by @StateManagerProvider to your module!"
      )
    }
    ZipStateManager.injectAgent(instance, owner)
  }

  fun openWatcher() {
    if (!FloatingPermission.canDrawOverlays(context)) {
      FloatingPermission.manageDrawOverlays(context)
      Toast.makeText(context, R.string.text_no_floating_window_permission, Toast.LENGTH_LONG)
          .show()
      return
    }
    if (pagesStack.isEmpty()) {
      Toast.makeText(context, "状态队列为空", Toast.LENGTH_LONG)
          .show()
      return
    }
    val stack = pagesStack.reversed()
        .map { it.javaClass.simpleName + "  当前状态：" + it.lifecycle.currentState }
        .toTypedArray()
    val dialog = AlertDialog.Builder(
        if (pagesStack.last() is Activity) pagesStack.last() as Activity else (pagesStack.last() as Fragment).activity!!
    )
        .setTitle("")
        .setItems(stack) { _, selected ->
          watchStateIdx = selected
          FloatyService.addWindow(floatyWindow)
        }
        .create()
    dialog.show()
  }

  fun getCurrentStateName(): String {
    try {
      ZipStateManager.getStateWrapInfo(pagesStack.reversed()[watchStateIdx])
          ?.binder?.getAgent(pagesStack.reversed()[watchStateIdx])
          ?.liveData?.value?.apply {
        return this.javaClass.simpleName
      }
    } catch (e: Exception) {
      return ""
    }
    return ""
  }

  fun getCurrentStateJsonString(): String {
    try {
      ZipStateManager.getStateWrapInfo(pagesStack.reversed()[watchStateIdx])
          ?.binder?.getAgent(pagesStack.reversed()[watchStateIdx])
          ?.liveData?.value?.apply {
        return JSONS.parseJson(this) ?: ""
      }
    } catch (e: Exception) {
      return ""
    }
    return ""
  }

  fun reloadState(json: String) {
    try {
      val observer = ZipStateManager.getStateWrapInfo(pagesStack.reversed()[watchStateIdx])
          ?.binder?.getAgent(pagesStack.reversed()[watchStateIdx])
      observer?.setStateFromJson(
          json
      )
    } catch (e: Exception) {
    }
  }
}