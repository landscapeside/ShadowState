package com.landside.shadowstate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.landside.shadowstate.ShadowState.StateType.PAGE
import com.landside.shadowstate.ShadowState.StateType.SHARE
import com.landside.shadowstate.watch.WatcherFloaty
import com.landside.shadowstate_annotation.BindState
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.stardust.enhancedfloaty.FloatyService
import com.stardust.enhancedfloaty.ResizableExpandableFloatyWindow
import com.stardust.enhancedfloaty.util.FloatingWindowPermissionUtil
import io.reactivex.subjects.PublishSubject
import java.lang.reflect.Type

object ShadowState {

  private enum class StateType{
    PAGE,SHARE
  }

  var pagesStack: MutableList<LifecycleOwner> = mutableListOf()
  private var watchStateIdx: Int = 0
  private var type:StateType = PAGE
  lateinit var context: Context
  private lateinit var floatyWindow: ResizableExpandableFloatyWindow
  internal val watchObjectPublisher: PublishSubject<String> = PublishSubject.create()

  val shareStates: MutableMap<Type, MutableLiveData<out Any>> = mutableMapOf()
  val attachStates:MutableMap<LifecycleOwner,MutableLiveData<out Any>> = mutableMapOf()

  fun init(
    context: Context,
    loggable: Boolean,
    useWatcher: Boolean = false,
    managers: Array<StateManager>
  ): ShadowState {
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
        Toast.makeText(
            context,
            R.string.text_no_floating_window_permission,
            Toast.LENGTH_LONG
        )
            .show()
      }
      context.startService(Intent(context, FloatyService::class.java))
    }
    ZipStateManager.zip(*managers)
    return this
  }

  fun <STATE : Any> setupShare(
    cls: Class<*>,
    state: STATE
  ): ShadowState {
    val liveData = MutableLiveData<STATE>()
    liveData.value = state
    shareStates[cls] = liveData
    return this
  }

  fun <STATE : Any>  setupAttach(
    instance:LifecycleOwner,
    state: STATE
  ): ShadowState{
    val liveData = MutableLiveData<STATE>()
    liveData.value = state
    attachStates[instance] = liveData
    return this
  }

  fun bind(lifecycleOwner: LifecycleOwner) {
    if (ZipStateManager.managers.isEmpty()) {
      throw IllegalStateException(
          "There is no stateManager!You need to create a class which is annotated by @StateManagerProvider into your module! "
      )
    }
    ZipStateManager.bind(lifecycleOwner)
    if (lifecycleOwner.javaClass.isAnnotationPresent(BindState::class.java)) {
      pagesStack.add(lifecycleOwner)
    }
  }

  fun rebind(lifecycleOwner: LifecycleOwner) {
    if (ZipStateManager.managers.isEmpty()) {
      throw IllegalStateException(
          "There is no stateManager!You need to add a class that is annotated by @StateManagerProvider to your module! "
      )
    }
    ZipStateManager.rebind(lifecycleOwner)
    if (lifecycleOwner.javaClass.isAnnotationPresent(BindState::class.java)) {
      pagesStack.remove(lifecycleOwner)
      pagesStack.add(lifecycleOwner)
    }
  }

  fun removePage(view: Any) {
    pagesStack.remove(view)
    ZipStateManager.remove(view as LifecycleOwner)
  }

  fun detachFragment(view: Any){
    ZipStateManager.detach(view as LifecycleOwner)
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
          type = PAGE
          watchStateIdx = selected
          FloatyService.addWindow(floatyWindow)
        }
        .create()
    dialog.show()
  }

  fun openShareWatcher() {
    if (!FloatingPermission.canDrawOverlays(context)) {
      FloatingPermission.manageDrawOverlays(context)
      Toast.makeText(context, R.string.text_no_floating_window_permission, Toast.LENGTH_LONG)
          .show()
      return
    }
    if (shareStates.isEmpty()) {
      Toast.makeText(context, "状态队列为空", Toast.LENGTH_LONG)
          .show()
      return
    }
    val shareStack = shareStates.keys.map { it.toString() }
        .toTypedArray()
    val dialog = AlertDialog.Builder(
        if (pagesStack.last() is Activity) pagesStack.last() as Activity else (pagesStack.last() as Fragment).activity!!
    )
        .setTitle("")
        .setItems(shareStack) { _, selected ->
          type = SHARE
          watchStateIdx = selected
          FloatyService.addWindow(floatyWindow)
        }
        .create()
    dialog.show()
  }

  fun getCurrentStateName(): String {
    try {
      if (type == PAGE) {
        ZipStateManager.getBinder(pagesStack.reversed()[watchStateIdx])
            ?.getAgent(pagesStack.reversed()[watchStateIdx])
            ?.liveData?.value?.apply {
          return this.javaClass.simpleName
        }
      } else {
        val shareStack = shareStates.keys
            .toTypedArray()
        return shareStack[watchStateIdx].toString()
      }
    } catch (e: Exception) {
      return ""
    }
    return ""
  }

  fun getCurrentStateJsonString(): String {
    try {
      if (type == PAGE) {
        ZipStateManager.getBinder(pagesStack.reversed()[watchStateIdx])
            ?.getAgent(pagesStack.reversed()[watchStateIdx])
            ?.liveData?.value?.apply {
          return JSONS.parseJson(this) ?: ""
        }
      } else {
        val shareStack = shareStates.keys
            .toTypedArray()
        shareStates[shareStack[watchStateIdx]]?.value?.apply {
          return JSONS.parseJson(this) ?: ""
        }
      }
    } catch (e: Exception) {
      return ""
    }
    return ""
  }

  fun reloadState(json: String) {
    try {
      if (type == PAGE) {
        val observer = ZipStateManager.getBinder(pagesStack.reversed()[watchStateIdx])
            ?.getAgent(pagesStack.reversed()[watchStateIdx])
        observer?.setStateFromJson(
            json
        )
      } else {
        val shareStack = shareStates.keys
            .toTypedArray()
        shareStates[shareStack[watchStateIdx]]?.value = JSONS.parseObject(json, shareStack[watchStateIdx])
      }
    } catch (e: Exception) {
    }
  }

  fun getAllStateJsonString(): List<String> {
    val result = mutableListOf<String>()
    try {
      pagesStack.reversed()
          .forEachIndexed { index, lifecycleOwner ->
            ZipStateManager.getBinder(pagesStack.reversed()[index])
                ?.getAgent(pagesStack.reversed()[index])
                ?.liveData?.value?.apply {
              result.add(
                  "name=>${this.javaClass.simpleName} state=>" + (JSONS.parseJson(this) ?: "")
              )
            }
          }
    } catch (e: Exception) {
      return result
    }
    return result
  }

  fun getAllShareJsonString(): List<String> {
    val result = mutableListOf<String>()
    try {
      shareStates.entries.forEach {
        result.add(
            "name=>${it.key} state=>${(JSONS.parseJson(it.value.value))}"
        )
      }
    } catch (e: Exception) {
      return result
    }
    return result
  }
}