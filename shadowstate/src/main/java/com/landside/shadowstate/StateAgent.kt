package com.landside.shadowstate

import android.arch.convert.RxJavaConvert
import android.os.Bundle
import androidx.lifecycle.*
import androidx.lifecycle.Lifecycle.Event
import com.orhanobut.logger.Logger
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.lifecycle.CorrespondingEventsFunction
import com.uber.autodispose.lifecycle.LifecycleEndedException
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.jetbrains.annotations.NotNull

abstract class StateAgent<STATE, VIEW> {

    inner class StateObserver : Observer<STATE>, LifecycleScopeProvider<Event>,
        LifecycleObserver {

        val liveData: MutableLiveData<STATE> = MutableLiveData()
        var view: VIEW? = null

        val lifecycleEvents =
            BehaviorSubject.createDefault(Lifecycle.Event.ON_ANY)

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy(@NotNull owner: LifecycleOwner) {
            lifecycleEvents.onNext(Lifecycle.Event.ON_DESTROY)
            stateObservers.remove(this)
            ShadowState.removePage(view!!)
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

        fun <SUB> listen(
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

        fun setStateFromJson(json: String, cls: Class<*>) {
            liveData.value = JSONS.parseObject(json, cls)
        }
    }

    val stateObservers: ArrayList<StateObserver> = arrayListOf()

    fun createObserver(): StateObserver {
        val observer = StateObserver()
        stateObservers.add(observer)
        return observer
    }

    fun init(observer: StateObserver) {
        conf()
        observer.lifecycleEvents.onNext(Lifecycle.Event.ON_CREATE)
    }

    fun bindView(view: VIEW, observer: StateObserver) {
        observer.view = view
        if (view is LifecycleOwner) {
            view.lifecycle.addObserver(observer)
        }
    }

    val view: VIEW?
        get() = try {
            stateObservers.last().view
        } catch (e: Exception) {
            null
        }


    abstract fun initState(bundle: Bundle?): STATE
    abstract fun conf()


    val state: STATE
        get() = try {
            stateObservers.last()
                ?.liveData?.value ?: initState(null)
        } catch (e: Exception) {
            initState(null)
        }

    fun setState(reducer: (STATE) -> STATE) {
        try {
            val stateData = stateObservers.last().liveData
            stateData.value?.run {
                stateData.value = reducer(this)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    fun postState(reducer: (STATE) -> STATE) {
        try {
            val stateData = stateObservers.last().liveData
            stateData.value?.run {
                stateData.postValue(reducer(this))
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    protected fun <SUB> listen(
        mapper: (STATE) -> SUB,
        executor: (SUB) -> Unit
    ) = listen(true, mapper, executor)

    protected fun <SUB> listen(
        distinct: Boolean = true,
        mapper: (STATE) -> SUB,
        executor: (SUB) -> Unit
    ) {
        try {
            stateObservers.last().listen(distinct, mapper, executor)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun <T, SUB> Observable<T>.subscribeSubState(
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

    private fun <T> LiveData<T>.toObservable() = RxJavaConvert.toObservable(this)
}