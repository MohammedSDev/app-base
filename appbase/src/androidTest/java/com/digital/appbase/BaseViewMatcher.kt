package com.digital.appbase

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.assertion.ViewAssertions
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.Matchers.not

@Suppress("UNCHECKED_CAST")
open class BaseViewMatcher<T : View>(
	private val desc: String = "",
	private val checkBlock: ((view: T) -> Boolean)? = null
) :
	TypeSafeMatcher<T>() {

	override fun describeTo(description: Description?) {
		description?.appendText(desc)
	}

	override fun matchesSafely(item: T) = checkBlock?.invoke(item) ?: false
}
open class BaseVHMatcher<T : RecyclerView.ViewHolder>(
	private val desc: String = "",
	private val checkBlock: ((view: T) -> Boolean)? = null
) :
	TypeSafeMatcher<T>() {

	override fun describeTo(description: Description?) {
		description?.appendText(desc)
	}

	override fun matchesSafely(item: T) = checkBlock?.invoke(item) ?: false
}

fun Matcher<out View>.matcher(): ViewAssertion = ViewAssertions.matches(this as Matcher<in View>)
fun Matcher<out View>.notMatcher(): ViewAssertion = ViewAssertions.matches(not(this) as Matcher<in View>)

