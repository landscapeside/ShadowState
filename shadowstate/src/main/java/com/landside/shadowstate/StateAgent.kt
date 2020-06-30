package com.landside.shadowstate

import android.arch.convert.RxJavaConvert
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.reactivex.Observable

abstract class StateAgent<STATE,VIEW>:Observer<STATE> {
    var stateObservable: MutableLiveData<STATE>? = null
        set(value) {
            field = value
            conf()
        }

    var view:VIEW?=null

    abstract fun initState(bundle: Bundle?):STATE
    abstract fun conf()

    final override fun onChanged(t: STATE) {
        t?.let {
            ShadowState.stateRecord(t)
        }
    }

    val state: STATE
        get() = stateObservable?.value ?: initState(null)

    fun setState(reducer: (STATE) -> STATE) {
        stateObservable?.value?.run {
            stateObservable?.value = reducer(this)
        }
    }

    fun postState(reducer: (STATE) -> STATE) {
        stateObservable?.value?.run {
            stateObservable?.postValue(reducer(this))
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
        stateObservable?.toObservable()
            ?.subscribeSubState(distinct = distinct, map = mapper)
            ?.skip(1)
            ?.subscribe {
                executor(it)
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