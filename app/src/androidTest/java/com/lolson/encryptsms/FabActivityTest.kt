package com.lolson.encryptsms


import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
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
class FabActivityTest
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
    fun fabActivityTest()
    {
        // Splash screen has a delay which must finish before anything else
        // Without it, resources aren't loaded properly
        SystemClock.sleep(1000)

        val floatingActionButton = onView(
            allOf(
                withId(R.id.fab),
                childAtPosition(
                    allOf(
                        withId(R.id.app_bar_main),
                        childAtPosition(
                            withId(R.id.drawer_layout),
                            0)),
                    2),
                isDisplayed()))
        floatingActionButton.perform(click())

        val toolbar = onView(
            allOf(
                withId(R.id.toolbar),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.app_bar_main),
                        0),
                    0),
                isDisplayed()))
        toolbar.perform(click())

        val appCompatButton = onView(
            allOf(
                withId(android.R.id.button2), withText("Cancel"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.buttonPanel),
                        0),
                    2)))
        appCompatButton.perform(scrollTo(), click())

        val overflowMenuButton = onView(
            allOf(
                withContentDescription("More options"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.toolbar),
                        1),
                    1),
                isDisplayed()))
        overflowMenuButton.perform(click())

        val appCompatTextView = onView(
            allOf(
                withId(R.id.title), withText("Settings"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.content),
                        0),
                    0),
                isDisplayed()))
        appCompatTextView.perform(click())

        val navigationMenuItemView = onView(
            allOf(
                withId(R.id.nav_vis),
                childAtPosition(
                    allOf(
                        withId(R.id.design_navigation_view),
                        childAtPosition(
                            withId(R.id.nav_view),
                            0)),
                    2),
                isDisplayed()))
        navigationMenuItemView.perform(click())

        val switchCompat = onView(
            allOf(
                withId(R.id.vis_switch_compat),
                childAtPosition(
                    allOf(
                        withId(R.id.vis_switch_rela_lay),
                        childAtPosition(
                            withId(R.id.design_menu_item_action_area),
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
                    0),
                isDisplayed()))
        appCompatImageButton.perform(click())
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
