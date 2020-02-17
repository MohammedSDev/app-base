package com.digital.appbase

import androidx.lifecycle.LiveData

class ShareLD {

	companion object{
		@JvmStatic
		val appLocaleLD:LiveData<String> = EventLiveData()


		@JvmStatic
		fun languageChange(lang:String){
			(appLocaleLD as EventLiveData).value = lang
		}
	}
}