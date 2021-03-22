package com.example.encryptsms


import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityWithEncryptTest
{

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule =
            GrantPermissionRule.grant(
                "android.permission.SEND_SMS",
                "android.permission.READ_CONTACTS",
                "android.permission.READ_SMS")

    @Test
    fun mainActivityWithEncryptTest()
    {
        val recyclerView = onView(
            allOf(
                withId(R.id.thread_list),
                childAtPosition(
                    withId(R.id.frameLayout),
                    0)))
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        val switchCompat = onView(
            allOf(
                withId(R.id.encrypt_switch_compat),
                childAtPosition(
                    allOf(
                        withId(R.id.switch_rela_lay),
                        childAtPosition(
                            withClassName(`is`("androidx.appcompat.widget.ActionMenuView")),
                            0)),
                    0),
                isDisplayed()))
        switchCompat.perform(click())

        val appCompatImageButton = onView(
            allOf(
                withContentDescription("Navigate up"),
                childAtPosition(
                    allOf(
                        withId(R.id.toolbar),
                        childAtPosition(
                            withClassName(`is`("com.google.android.material.appbar.AppBarLayout")),
                            0)),
                    1),
                isDisplayed()))
        appCompatImageButton.perform(click())

        val recyclerView2 = onView(
            allOf(
                withId(R.id.thread_list),
                childAtPosition(
                    withId(R.id.frameLayout),
                    0)))
        recyclerView2.perform(actionOnItemAtPosition<ViewHolder>(0, click()))
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int): Matcher<View>
    {

        return object : TypeSafeMatcher<View>()
        {
            override fun describeTo(description: Description)
            {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean
            {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
