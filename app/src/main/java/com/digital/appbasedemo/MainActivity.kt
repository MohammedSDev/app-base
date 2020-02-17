package com.digital.appbasedemo

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.digital.appbase.AppActivity

class MainActivity : AppActivity() {
	override fun getUserLanguage(context: Context?): String {
		return ""
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
	}
}
