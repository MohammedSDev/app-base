package com.digital.appbase

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import java.io.Serializable
import java.lang.IllegalArgumentException

/**
 * Created by Gg on 2/9/2019.
 */
abstract class AppBaseFragment : Fragment() {

	var fragView: View? = null
	var isNewViewCreated = false
	internal var nestedFrag: AppBaseFragment? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		AppBaseSharedContext.context = activity?.application

		onCreate()
		configObserves()

	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		if (fragView == null) {
			fragView = inflater.inflate(onCreateView(), container, false)
			isNewViewCreated = true

		}
		return fragView
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)


		if (isNewViewCreated) {
			configUi()
			isNewViewCreated = false
		}
	}

	open fun onCreate() {}
	abstract fun onCreateView(): Int
	open fun configUi() {}
	open fun configObserves() {}


	/**
	 * get Bundle
	 * */
	var bundle: Bundle
		get() = if (arguments != null) arguments!! else {
			arguments = Bundle()
			arguments!!
		}
		set(value) {
			arguments = value
		}


	fun startActivity(target: Class<*>, bundle: Bundle? = null, options: Bundle? = null) {
		startActivity(Intent(context, target).also {
			if (bundle != null)
				it.putExtras(bundle)
		}, options)
	}

	fun startActivity(target: Class<*>, options: Bundle? = null, vararg c: Pair<String, Any>?) {
		val bundle = Bundle()
		c.forEach {
			when (val second = it?.second) {
				is Int -> bundle.putInt(it.first, second)
				is String -> bundle.putString(it.first, second)
				is Float -> bundle.putFloat(it.first, second)
				is Double -> bundle.putDouble(it.first, second)
				is Serializable -> bundle.putSerializable(it.first, second)
				else -> throw IllegalArgumentException(
					"---- error ----- " +
						"${second?.javaClass} is not handled type.instead use basic startActivity with Intent."
				)
			}
		}
		startActivity(Intent(context, target).also {
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
		childFragmentManager.beginTransaction()
			.replace(containerId, fragment.also { it.arguments = bundle }, tag).also {
				if (addToBackStack)
					it.addToBackStack(fragment.toString())
			}
			.commit()
	}

	fun getColorComp(colorRes: Int): Int {
		return if (activity?.isFinishing == false)
			ContextCompat.getColor(context!!, colorRes)
		else
			0
	}

	fun getDrawableComp(drawableRes: Int): Drawable? {
		return if (activity?.isFinishing == false)
			ContextCompat.getDrawable(context!!, drawableRes)
		else
			null
	}

	fun getDimenComp(dimenRes: Int) = resources.getDimension(dimenRes)

	//region LifeCycle
	//endregion


	//// -------
	fun isContextAvailable(): Boolean {
		return activity != null && activity?.isFinishing == false
	}

	fun delay(delay: Long, callback: () -> Unit) = Handler().postDelayed(callback, delay)


	/**
	 * enable handling on parent Fragment onBackPressed
	 * */
	fun enableNestedOnBackPressed() {
		(parentFragment as? AppBaseFragment)?.nestedFrag = this
		enableOnBackPressed()
	}

	/**
	 * enable handling activity onBackPressed
	 * */
	fun enableOnBackPressed() {
		if (activity is AppBaseActivity) {
			(activity as AppBaseActivity).nestedFrag = this
		}
	}

	/**
	 * handle onBackPressed.
	 * @return true: if you handle onBackPressed,
	 * false: to let app handle onBackPressed
	 * */
	open fun onBackPressed(): Boolean {
		return nestedFrag?.onBackPressed() == true
	}
}