package com.digital.appbase

import android.app.Application
import android.content.Context
import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.digital.appktx.changeAppLocale

/**
 * App Application class
 * */
abstract class AppAppBase : Application() {

	open fun applyLanguageToBaseContext(): Boolean = true

	override fun attachBaseContext(base: Context?) {
		val langCode = getUserLanguage(base)
		if (applyLanguageToBaseContext())
			super.attachBaseContext(changeAppLocale(langCode, base, {}))
		else
			super.attachBaseContext(base)
	}

	/**
	 * return default or runtime app language code.{e.g: ar,en,fr,..ext}
	 * snipe  e.g: if (@param context != null) getSavedUserLanguage() else Locale.getDefault().language
	 * */
	abstract fun getUserLanguage(context: Context?): String

	@CallSuper
	override fun onCreate() {
		super.onCreate()
		AppBaseSharedContext.context = this
	}

	/**
	 * return instance of your view model class.
	 * e,g: modelClass.isAssignableFrom(YourViewModel::class.java) -> {
	return YourViewModel(application, Repository())
	}
	 * */
	open fun <T : ViewModel?> createVM(modelClass: Class<T>): ViewModel = EmptyVM(this)
}