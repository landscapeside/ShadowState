package com.landside.shadowstate

import android.arch.convert.RxJavaConvert
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.orhanobut.logger.Logger
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import com.uber.autodispose.lifecycle.LifecycleEndedException
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.jetbrains.annotations.NotNull
import java.lang.reflect.Type

abstract class ShadowStateAgent<STATE : Any, VIEW> : Observer<STATE>, LifecycleScopeProvider<Event>,
    LifecycleObserver {

  var liveData: MutableLiveData<STATE> = MutableLiveData()
  var view: VIEW? = null
  lateinit var stateCls: Class<*>

  val lifecycleEvents =
    BehaviorSubject.createDefault(Lifecycle.Event.ON_ANY)

  @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
  fun onDestroy(@NotNull owner: LifecycleOwner) {
    lifecycleEvents.onNext(Lifecycle.Event.ON_DESTROY)
    ShadowState.removePage(view!!)
    view = null
    liveData.removeObserver(this)
  }

  override fun lifecycle(): Observable<Lifecycle.Event> {
    return lifecycleEvents.hide()
  }

  private val CORRESPONDING_EVENTS = CorrespondingEventsFunction<Lifecycle.Event> { event ->
    when (event) {
      Lifecycle.Event.ON_CREATE -> Lifecycle.Event.ON_DESTROY
      Lifecycle.Event.ON_ANY -> Lifecycle.Event.ON_DESTROY
      else -> throw LifecycleEndedException(
          "Cannot bind to ViewModel lifecycle after onCleared."
      )
    }
  }

  override fun correspondingEvents(): CorrespondingEventsFunction<Event> {
    return CORRESPONDING_EVENTS
  }

  override fun peekLifecycle(): Lifecycle.Event? {
    return lifecycleEvents.value
  }

  override fun onChanged(t: STATE) {
    t?.let {
      ShadowState.stateRecord(t)
      ShadowState.watchRecord(t)
    }
  }

  fun setStateFromJson(
    json: String
  ) {
    liveData.value = JSONS.parseObject(json, stateType())
  }

  open fun stateType(): Type = liveData.value!!.javaClass

  fun init() {
    conf()
    lifecycleEvents.onNext(Lifecycle.Event.ON_CREATE)
  }

  fun bindView(
    view: VIEW
  ) {
    this.view = view
    if (view is LifecycleOwner) {
      liveData.observe(view,this)
      view.lifecycle.addObserver(this)
    }
  }

  abstract fun conf()

  open val state: STATE
    get() = try {
      liveData.value ?: stateCls.newInstance() as STATE
    } catch (e: Exception) {
      stateCls.newInstance() as STATE
    }

  fun setState(reducer: (STATE) -> STATE) {
    try {
      liveData.value?.run {
        liveData.value = reducer(this)
      }
    } catch (e: Exception) {
      // ignore
    }
  }

  fun postState(reducer: (STATE) -> STATE) {
    try {
      liveData.value?.run {
        liveData.postValue(reducer(this))
      }
    } catch (e: Exception) {
      // ignore
    }
  }

  protected fun <SUB> listen(
    mapper: (STATE) -> SUB,
    executor: (SUB) -> Unit
  ) = listen(distinct = true, mapper = mapper, executor = executor)

  protected fun <SUB> listen(
    distinct: Boolean = true,
    mapper: (STATE) -> SUB,
    executor: (SUB) -> Unit
  ) {
    liveData.toObservable()
        .subscribeSubState(distinct = distinct, map = mapper)
        .skip(1)
        .autoDisposable(this)
        .subscribe(
            {
              executor(it)
            }, {
          Logger.e(it, "")
        }
        )
  }

  protected fun <T, SUB> Observable<T>.subscribeSubState(
    distinct: Boolean = true,
    map: (T) -> SUB
  ): Observable<SUB> {
    val observable = this.filter {
      map(it) != null
    }
        .map {
          map(it)
        }
    return if (distinct) observable
        .distinctUntilChanged() else observable
  }

  protected fun <T> LiveData<T>.toObservable() = RxJavaConvert.toObservable(this)
}