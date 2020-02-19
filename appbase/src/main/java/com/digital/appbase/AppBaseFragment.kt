package com.digital.appbase

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat

/**
 * Created by Gg on 2/9/2019.
 */
abstract class AppBaseFragment : Fragment() {

	var fragView: View? = null
	var isNewViewCreated = false

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
	var bundle:Bundle
		get() = if (arguments != null) arguments!! else {
			arguments = Bundle()
			arguments!!
		}
		set(value) {
			arguments = value
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

	fun delay(delay:Long,callback: ()->Unit) = Handler().postDelayed(callback,delay)

}