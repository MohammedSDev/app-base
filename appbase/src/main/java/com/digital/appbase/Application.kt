package com.digital.appbase

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import com.digital.appktx.changeAppLocale

/**
 * App Application class
 * */
abstract class AppApp : Application() {


	override fun attachBaseContext(base: Context?) {
		val langCode = getUserLanguage(base)
		super.attachBaseContext(changeAppLocale(langCode, base, {}))
	}
	/**
	 * return default or runtime app language code.{e.g: ar,en,fr,..ext}
	 * snipe  e.g: if (@param context != null) getSavedUserLanguage() else Locale.getDefault().language
	 * */
	abstract fun getUserLanguage(context: Context?):String


	override fun onCreate() {
		super.onCreate()
		AppSharedContext.context = this
	}

	/**
	 * return instance of your view model class.
	 * e,g: modelClass.isAssignableFrom(YourViewModel::class.java) -> {
					return YourViewModel(application, Repository())
					}
	 * */
	abstract fun <T: ViewModel?>createVM(modelClass: Class<T>):ViewModel
}