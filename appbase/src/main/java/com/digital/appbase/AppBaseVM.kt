package com.digital.appbase

import android.app.Application
import android.content.Context
import android.os.Handler
import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.digital.appktx.changeAppLocale
import java.util.*

abstract class AppBaseVM(application: Application) : AndroidViewModel(application) {


	//	protected open val mComb = AppCompositeDisposable()
	val mDelayCallbackLD: LiveData<Any?> = MutableLiveData()

	init {
		AppBaseSharedContext.context = application
	}

	fun requestCallbackDelay(delay: Long, tag: Any? = null) {
		Handler().postDelayed({
			getMutableLiveData(mDelayCallbackLD).postValue(tag)
		}, delay)
	}

	override fun onCleared() {
		super.onCleared()
//		mComb.cancelAll()

	}

	/**
	 * functionality to cast liveData as MutableLiveData
	 * */
	fun <L> getMutableLiveData(liveData: LiveData<L>): MutableLiveData<L> =
		liveData as MutableLiveData<L>

	fun getString(@StringRes strRes: Int) = getApplication<Application>().getString(strRes)

	open fun applyLocaleContext() = false

	fun getContext(langCode: String = Locale.getDefault().language): Context {
		return if (applyLocaleContext()) changeAppLocale(
			langCode,
			getApplication(),
			{}) else getApplication()
	}
//	@VisibleForTesting
//	fun getCompositeDisposable() = mComb
}