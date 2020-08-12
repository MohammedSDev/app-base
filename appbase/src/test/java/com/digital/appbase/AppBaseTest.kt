package com.digital.appbase

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.digital.appktx.serialize
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.net.HttpURLConnection


@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
abstract class BaseTest {

	val appContext: Application
		get() = ApplicationProvider.getApplicationContext()
	private var mockServerInit: Boolean = false
	private val server: MockWebServer by lazy {
		mockServerInit = true
		MockWebServer().also {
			it.start()
			onMockServerStart()
		}
	}

	fun addResponse(body: String) {
		server.enqueue(
			MockResponse()
				.setBody(body)
		)
	}

	fun addResponse(body: Any) {
		server.enqueue(
			MockResponse()
				.setBody(body.serialize())
		)
	}

	fun addErrorResponse(body: String = "") {
		server.enqueue(
			MockResponse()
				.setResponseCode(HttpURLConnection.HTTP_UNAVAILABLE)
				.setBody(body)
		)
	}

	fun getRequestPath(): String = server.takeRequest().path

	//
	fun getString(@StringRes res: Int) = appContext.getString(res)

	open fun onMockServerStart() {
		//			RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
//			RetrofitObject.changeBaseUrl(it.url("").url().toString())
	}

	/**
	 * return same SharedPreferences used in test..if there any.
	 * */
	open fun getSP(context: Context): SharedPreferences? = null


	@Before
	@CallSuper
	open fun before() {

	}

	@After
	@CallSuper
	open fun after() {
		getSP(appContext)?.edit()?.clear()?.commit()
		if (mockServerInit)
			server.shutdown()
	}
}