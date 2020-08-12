package com.digital.appbase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.digital.appktx.serialize
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@LargeTest
open class BaseViewTest(
	private val testAc: Class<out AppBaseActivity>? = null,
	private val autoOpenAc: Boolean = true,
	testFrag: Class<out Fragment>? = null
) {
	constructor() : this(null, false, null)

	@get:Rule
	val activityRule = ActivityTestRule(testAc, false, false)

	val appContext: Context
		get() = activityRule.activity ?: InstrumentationRegistry.getInstrumentation().targetContext

	private var mockServerInit: Boolean = false
	private val server: MockWebServer by lazy {
		mockServerInit = true
		MockWebServer().also {
			it.start()
			onMockServerStart()
		}
	}

	open fun onMockServerStart() {
//		RetrofitObject.changeBaseUrl(it.url("").url().toString())
	}



	fun openActivity(bundle: Bundle? = null) {
//		val i = activityRule.activity.intent?.also { it.putExtras(bundle) }
		val i = Intent(
			InstrumentationRegistry.getInstrumentation().targetContext,
			testAc
		)?.also { if (bundle != null) it.putExtras(bundle) }
		activityRule.launchActivity(i)
	}



	fun getRequestsCount() = server.requestCount

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

	fun waitFor(t: Long = 1000) {
		Thread.sleep(t)
	}

	@Deprecated("use openFragment")
	fun openFragmentScenario(testFragment: Fragment) {
		ActivityScenario.launch(FragmentTestActivity::class.java).onActivity {
			it.startFragment(
				android.R.id.content,
				testFragment,
				bundle = testFragment.arguments ?: Bundle()
			)
		}
	}

	fun openFragment(testFragment: Fragment) {
		val i = Intent(
			InstrumentationRegistry.getInstrumentation().targetContext,
			FragmentTestActivity::class.java
		)
		activityRule.launchActivity(i)
		activityRule.activity.startFragment(
			android.R.id.content,
			testFragment,
			bundle = testFragment.arguments ?: Bundle()
		)
	}

	/**
	 * return same SharedPreferences used in test..if there any.
	 * */
	open fun getSP(context: Context): SharedPreferences? = null

	@Before
	@CallSuper
	open fun before() {
		Intents.init()
		if (autoOpenAc) openActivity()
	}

	@After
	@CallSuper
	open fun clear() {
		getSP(appContext)?.edit()?.clear()?.commit()
		Intents.release()
		if (mockServerInit)
			server.shutdown()
	}

	//
	fun getString(@StringRes res: Int) = appContext.getString(res)

	fun checkActivityLaunched(activity: Class<out Activity>) =
		Intents.intended(IntentMatchers.hasComponent(activity.name))

	fun onView(@IdRes viewId: Int) = onView(withId(viewId))
	fun on(@IdRes viewId: Int) = onView(withId(viewId))
	fun on(viewMatcher: Matcher<View>) = onView(viewMatcher)

	fun clickOn(@IdRes viewId: Int) =
		onView(withId(viewId)).perform(scrollTo(), click())

//	fun performOn(@IdRes viewId: Int, vararg viewActions: ViewAction) =
//		onView(withId(viewId)).perform(*viewActions)
//
//	fun checkOn(@IdRes viewId: Int, viewAssert: ViewAssertion) =
//		onView(withId(viewId)).check(viewAssert)

	fun performOn(@IdRes viewId: Int, action: ViewAction): ViewAction = object : ViewAction {
		override fun getDescription(): String {
			return ""
		}

		override fun getConstraints(): Matcher<View> {
			return BaseViewMatcher()
		}

		override fun perform(uiController: UiController?, view: View?) {
			action.perform(uiController, view?.findViewById(viewId))
		}
	}

	fun checkOn(@IdRes viewId: Int, assertion: ViewAssertion) = ViewAssertion { view, noViewEx ->
		assertion.check(view.findViewById(viewId), noViewEx)
	}

	fun allOf(vararg viewActions: Matcher<View>): Matcher<View> = object : BaseMatcher<View>() {
		override fun describeTo(description: Description?) {
		}

		override fun matches(item: Any?): Boolean {
			viewActions.forEach { if (!it.matches(item)) return false }
			return true
		}

	}

	fun allOf(vararg viewActions: ViewAction): ViewAction = object : ViewAction {
		override fun getDescription(): String {
			return ""
		}

		override fun getConstraints(): Matcher<View> {
			return BaseViewMatcher()
		}

		override fun perform(uiController: UiController?, view: View?) {
			viewActions.forEach { it.perform(uiController, view) }
		}
	}

	fun allOf(vararg viewAssertions: ViewAssertion): ViewAssertion =
		ViewAssertion { view, noViewFoundException ->
			viewAssertions.forEach {
				it.check(
					view,
					noViewFoundException
				)
			}
		}

	fun onRecyclerAtPosition(pos: Int, action: ViewAction) =
		RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(pos, action)

	fun onRecyclerRow(matcher: Matcher<View>, action: ViewAction) =
		RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(matcher, action)

	fun onRecyclerAtPosition(pos: Int, assertion: ViewAssertion): ViewAssertion =
		ViewAssertion { view, _ ->
			if (view !is RecyclerView) throw AssertionError("$view was not RecyclerView.")
			val vh = view.findViewHolderForAdapterPosition(pos)
				?: throw AssertionError("no row found for position $pos in $view")
			assertion.check(vh.itemView, null)
		}

	fun onRecyclerRow(matcher: Matcher<View>, assertion: ViewAssertion) =
		ViewAssertion { view, _ ->
			if (view !is RecyclerView) throw AssertionError("$view was not RecyclerView.")

			var rowView: View? = null
			run {
				val vh = view.children?.forEach {
					if (matcher.matches(it))
						rowView = it
					return@run
				}
			}
			rowView ?: throw AssertionError("no row found for matcher $matcher in $view")
			assertion.check(rowView, null)
		}

}

