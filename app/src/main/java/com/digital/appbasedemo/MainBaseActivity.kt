package com.digital.appbasedemo

import android.content.Context
import android.os.Bundle
import com.digital.appbase.AppBaseActivity

class MainBaseActivity : AppBaseActivity() {
	override fun getUserLanguage(context: Context?): String {
		return ""
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
	}
}
