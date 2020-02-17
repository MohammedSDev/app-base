package com.digital.appbase

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.digital.appktx.removeItemIf
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class EventLiveData<T> : MediatorLiveData<T>() {


	private val observers = ConcurrentHashMap<LifecycleOwner, MutableList<ObserverWrapper<in T>>>()

	@MainThread
	override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
		val wrapper = ObserverWrapper(observer)
		val set = observers[owner]
		set?.apply {
			add(wrapper)
		} ?: run {
			val newSet = mutableListOf<ObserverWrapper<in T>>()
			newSet.add(wrapper)
			observers[owner] = newSet
		}
		super.observe(owner, wrapper)
	}

	override fun removeObservers(owner: LifecycleOwner) {
		observers.remove(owner)
		super.removeObservers(owner)
	}

	override fun removeObserver(observer: Observer<in T>) {
		var owner: LifecycleOwner? = null
		var mutSet: MutableList<ObserverWrapper<in T>>? = null
		observers.forEach {
			val wrapper = it.value.toList().find { it.observer == observer }
			if (wrapper != null) {
				owner = it.key
				mutSet = it.value
				return@forEach
			}
		}
		mutSet?.removeItemIf { it.observer == observer }
		if (mutSet?.isEmpty() == true) {
			owner?.let { removeObservers(it) }
		}
		super.removeObserver(observer)
	}

	@MainThread
	override fun setValue(t: T?) {
		observers.forEach { it.value.forEach { wrapper -> wrapper.newValue() } }
		super.setValue(t)
	}

	/**
	 * Used for cases where T is Void, to make calls cleaner.
	 */
	@MainThread
	fun clear() {
		value = null
	}

	private class ObserverWrapper<T>(val observer: Observer<T>) : Observer<T> {

		private val pending = AtomicBoolean(false)

		override fun onChanged(t: T?) {
			if (pending.compareAndSet(true, false)) {
				observer.onChanged(t)
			}
		}

		fun newValue() {
			pending.set(true)
		}
	}
}