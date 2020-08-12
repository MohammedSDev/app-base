package com.digital.appbase


import android.os.Looper
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.digital.appktx.getAs
import com.digital.appktx.removeFirstItemIf
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.robolectric.Shadows
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun <T> LiveData<T>.getOrAwaitValue(
	time: Long = 2,
	timeUnit: TimeUnit = TimeUnit.SECONDS,
	afterObserve: () -> Unit = {}
): T {
	var data: T? = null
	val latch = CountDownLatch(1)
	val observer = object : Observer<T> {
		override fun onChanged(o: T?) {
			data = o
			latch.countDown()
			this@getOrAwaitValue.removeObserver(this)
		}
	}
	this.observeForever(observer)
	try {
		// Don't wait indefinitely if the LiveData is not set.
		if (!latch.await(time, timeUnit)) {
			throw TimeoutException("LiveData value was never set.")
		}
	} finally {
		this.removeObserver(observer)
		afterObserve.invoke()
	}
	@Suppress("UNCHECKED_CAST")
	return data as T
}

@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun <T> LiveData<T>.getOrAwaitValue(
	time: Long = 2,
	timeUnit: TimeUnit = TimeUnit.SECONDS,
	afterObserve: () -> Unit = {},
	defaultValue: T?
): T? {
	var data: T? = null
	val latch = CountDownLatch(1)
	val observer = object : Observer<T> {
		override fun onChanged(o: T?) {
			data = o
			latch.countDown()
			this@getOrAwaitValue.removeObserver(this)
		}
	}
	this.observeForever(observer)
	try {
		// Don't wait indefinitely if the LiveData is not set.
		if (!latch.await(time, timeUnit)) {
			return defaultValue
		}
	} finally {
		this.removeObserver(observer)
		afterObserve.invoke()
	}
	@Suppress("UNCHECKED_CAST")
	return data as T
}

@VisibleForTesting
fun <T> LiveData<PagedList<T>>.startAwaitingValue(delay:Long = 2000) {
	if (testPagedListObs.find { it.pagedList == this } == null) {
		val o = Observer<PagedList<T>> {}
		observeForever(o)
		testPagedListObs.add(TestPagedObs(this, o))
		waitFor(delay)
	}
}


@VisibleForTesting
fun <T> LiveData<PagedList<T>>.stopAwaitingValue() {
	testPagedListObs.removeFirstItemIf { it.pagedList == this }?.let {
		removeObserver(getAs<Observer<in PagedList<T>>>(it.observer)!!)
	}
}

@VisibleForTesting
fun waitFor(t: Long = 1000) {
	Thread.sleep(t)
	Shadows.shadowOf(Looper.getMainLooper()).idle()
}
///Matchers
/**
 * Equals Matcher
 * */
class EqMatcher<T>(val expected: T) : BaseMatcher<T?>() {
	var item: Any? = null
	override fun describeTo(description: Description?) {
		description?.appendText(" expected $expected , but was $item .")
	}

	override fun matches(item: Any?): Boolean {
		this.item = item
		return expected!!.equals(item)
	}
}


private data class TestPagedObs(val pagedList: LiveData<out Any>, val observer: Observer<out Any>)

private val testPagedListObs = mutableListOf<TestPagedObs>()



//ui


fun <T> matchThat(check: (T) -> Boolean) = object : TypeSafeMatcher<T>() {
	override fun describeTo(description: Description?) {
		description?.appendText("matchThat's checkBlock.")
	}

	override fun matchesSafely(item: T): Boolean = check(item)

}

fun <T : View> assertViewThat(desc: String = "", check: (T) -> Boolean) =
	BaseViewMatcher(desc, check).matcher()



fun isInputLayoutWithError(
	errorMessage: String,
	desc: String = "Input Layout error $errorMessage"
) =
	BaseViewMatcher<TextInputLayout>(desc) {
		it.error == errorMessage
	}.matcher()


fun androidx.test.espresso.action.ViewActions.clearFocus(): ViewAction = object : ViewAction {
	override fun getDescription() = "loss view focus"


	override fun getConstraints(): Matcher<View> = ViewMatchers.isEnabled()

	override fun perform(uiController: UiController?, view: View?) {
		if (view?.isFocused == true) {
			view?.clearFocus()
			uiController?.loopMainThreadUntilIdle()
		}
	}

}