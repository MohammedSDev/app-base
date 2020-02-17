package com.digital.appbase

import android.app.Application
import java.lang.ref.WeakReference


object AppSharedContext {

	private var context_: WeakReference<Application?> = WeakReference(null)


	var context: Application?
		set(value) {
			if (context_.get() == null)
				context_ = WeakReference(value)
		}
		get() = context_.get()

}