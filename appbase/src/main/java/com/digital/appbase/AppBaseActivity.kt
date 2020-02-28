package com.digital.appbase

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.digital.appktx.changeAppLocale
import com.digital.appktx.getCurrentLanguageCode
import java.io.Serializable
import java.lang.IllegalArgumentException


/**
 * Created by Gg on 2/7/2019.
 *
 * dot's use this class as direct parent of your activities.
 * instead use it as parent of your base activity class.
 */
abstract class AppBaseActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		AppBaseSharedContext.context = application
		//mack content behind status bar
		//window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		changeDecorateViewLayoutDirection()
		AppBaseShareLD.appLocaleLD.observe(this, androidx.lifecycle.Observer {
			handleLanguageChangeEvent()
		})


		onCreate()
		onCreateActivity(savedInstanceState)
		configUi()
		configObserves()

	}

	/**
	 * handle language change event.
	 *
	 * */
	protected open fun handleLanguageChangeEvent() {
		recreate()
	}


	//region lifeCycle & configuration Methods
	/**
	 * should override this method to set your layout view & viewmode
	 * */
	open fun onCreate() {}

	open fun onCreateActivity(savedInstanceState: Bundle?) {}


	/**
	 * should override this method and contain all your observers here
	 * */
	open fun configObserves() {}

	/**
	 * should override this method and contain all config & prepare views things here
	 * */
	open fun configUi() {}
	//endregion

	//region usefulMethods

	/**
	 * get current language code
	 * */
	open fun getCurrentLanguageCode(): String {
		return getCurrentLanguageCode(this)
	}


	/**
	 * change decorateView layout direction depending on current language
	 * */
	protected open fun changeDecorateViewLayoutDirection() {
		if (getCurrentLanguageCode().equals("ar", true)) {
			window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
		} else {
			window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
		}
	}

	//  <item name="android:windowLightStatusBar" tools:targetApi="23">true</item>
	@TargetApi(Build.VERSION_CODES.M)
	fun changeStatusBarIconColor(toDark: Boolean) {
		val decor = window.decorView
		if (toDark) {
			decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
		} else {
			// We want to change tint color to white again.
			// You can also record the flags in advance so that you can turn UI back completely if
			// you have set other flags before, such as translucent or full screen.
			decor.systemUiVisibility = 0
		}
	}

	/**
	 * get Bundle
	 * */
	val bundle: Bundle
		get() = intent.extras ?: Bundle()


	fun changeStatusBarColor(color: Int) {
		runCatching {
			// clear FLAG_TRANSLUCENT_STATUS flag:
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

// finally change the color
			window.setStatusBarColor(color)
		}
	}

	fun startActivity(target: Class<*>, bundle: Bundle? = null, options: Bundle? = null) {
		startActivity(Intent(this, target).also {
			if (bundle != null)
				it.putExtras(bundle)
		}, options)
	}

	fun startActivity(target: Class<*>, options: Bundle? = null, vararg c: Pair<String, Any>?) {
		val bundle = Bundle()
		c.forEach {
			when(val second = it?.second){
				is Int -> bundle.putInt(it.first,second)
				is String -> bundle.putString(it.first,second)
				is Float -> bundle.putFloat(it.first,second)
				is Double -> bundle.putDouble(it.first,second)
				is Serializable -> bundle.putSerializable(it.first,second)
				else -> throw IllegalArgumentException("---- error ----- " +
					"${second?.javaClass} is not handled type.instead use basic startActivity with Intent.")
			}
		}
		startActivity(Intent(this, target).also {
				it.putExtras(bundle)
		}, options)
	}

	fun startFragment(
		containerId: Int,
		fragment: Fragment,
		tag: String? = null,
		bundle: Bundle,
		addToBackStack: Boolean = false
	) {
		supportFragmentManager.beginTransaction()
			.replace(containerId, fragment.also { it.arguments = bundle }, tag).also {
				if (addToBackStack)
					it.addToBackStack(fragment.toString())
			}
			.commit()
	}

	fun getColorComp(colorRes: Int) = ContextCompat.getColor(this, colorRes)
	fun getDrawableComp(drawableRes: Int) = ContextCompat.getDrawable(this, drawableRes)
	fun getDimenComp(dimenRes: Int) = resources.getDimension(dimenRes)

	//endregion

	/**
	 * return default or runtime app language code.{e.g: ar,en,fr,..ext}
	 * snipe  e.g: if (@param context != null) getSavedUserLanguage() else Locale.getDefault().language
	 * */
	abstract fun getUserLanguage(context: Context?): String

	override fun attachBaseContext(newBase: Context?) {
		val langCode = getUserLanguage(newBase)
		super.attachBaseContext(changeAppLocale(langCode, newBase, {}))
	}

	fun delay(delay: Long, callback: () -> Unit) = Handler().postDelayed(callback, delay)
}

/**
 * todo
 * put for bundle
 * */