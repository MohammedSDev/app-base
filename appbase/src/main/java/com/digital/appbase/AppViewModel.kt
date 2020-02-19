package com.digital.appbase

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import java.lang.IllegalArgumentException
import kotlin.reflect.KProperty


inline fun <reified T : AppBaseVM> Fragment.createVM(shareVM: Boolean = false): FragmentVM<T> {
	return FragmentVM<T>(T::class.java, shareVM)
}

inline fun <reified T : AppBaseVM> Fragment.createParentVM(): FragmentVM<T> {

	return FragmentVM(T::class.java, false, parentFrag = true)
}

inline fun <reified T : AppBaseVM> AppBaseActivity.createVM(): ActivityVM<T> {
	return ActivityVM<T>(T::class.java)
}

class FragmentVM<T : AppBaseVM>(
	val c: Class<T>,
	val shareVM: Boolean = false,
	val parentFrag: Boolean = false
) {
	operator fun getValue(thisRef: Fragment, property: KProperty<*>): T {
		return if (shareVM)
			ViewModelProviders.of(
				thisRef.requireActivity(),
				ViewModelFactory(thisRef.requireActivity().application)
			).get(c)
		else if (parentFrag)
			ViewModelProviders.of(
				thisRef.parentFragment!!,
				ViewModelFactory(thisRef.requireActivity().application)
			).get(c)
		else
			ViewModelProviders.of(
				thisRef,
				ViewModelFactory(thisRef.requireActivity().application)
			).get(c)
	}
}

class ActivityVM<T : AppBaseVM>(val c: Class<T>) {
	operator fun getValue(thisRef: AppCompatActivity, property: KProperty<*>): T {
		return ViewModelProviders.of(
			thisRef,
			ViewModelFactory(thisRef.application)
		).get(c)
	}
}

/**
 * ViewModelFactory with @param applications
 * */
open class ViewModelFactory(private val application: Application) :
	ViewModelProvider.NewInstanceFactory() {


	override fun <T : ViewModel?> create(modelClass: Class<T>): T {
		return if(application is AppApp)
			application.createVM(modelClass) as T
		else
			throw IllegalArgumentException("---- Error ---- " +
				"Your Application class must be extent from `AppApp` class. " +
				"-----  note: check hot use custom Application class in android. ---")
	}
}