package sword.notes.android

import android.content.Context
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.NoMatchingViewException
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.RootMatchers.withDecorView
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorActivityTest {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule<NoteListActivity>(NoteListActivity::class.java)

    private fun Context.clickMenuItem(text: String) {
        try {
            onView(ViewMatchers.withText(text)).perform(ViewActions.click())
        }
        catch (e: NoMatchingViewException) {
            Espresso.openActionBarOverflowOrOptionsMenu(this)
            onView(ViewMatchers.withText(text)).perform(ViewActions.click())
        }
    }

    private fun withTitle(title: String): TypeSafeMatcher<NoteListItem> {
        return object : TypeSafeMatcher<NoteListItem>(NoteListItem::class.java) {
            override fun describeTo(description: Description?) {
                // Not implemented
            }

            override fun matchesSafely(item: NoteListItem): Boolean = item.title == title
        }
    }

    private fun assertToastPresent(textResId: Int) {
        onView(withText(textResId)).inRoot(withDecorView(not(activityTestRule.activity.window.decorView))).check(matches(isDisplayed()))
    }

    @Test
    fun createNote() {
        val activity = activityTestRule.activity
        activity.clickMenuItem(activity.getString(R.string.optionNew))

        val noteTitle = "My test note"
        val noteContent = "This is my note content"

        onView(withText(activity.getString(R.string.createDialogTitle))).check(matches(isDisplayed()))
        onView(withId(R.id.textField)).perform(replaceText(noteTitle))
        onView(withText(activity.getString(android.R.string.yes))).check(matches(isDisplayed())).perform(click())

        onData(withTitle(noteTitle)).perform(click())
        onView(withId(R.id.textField)).check(matches(isDisplayed())).perform(replaceText(noteContent))
        activity.clickMenuItem(activity.getString(R.string.optionSave))
        assertToastPresent(R.string.saveFeedback)
        Espresso.pressBack()

        onData(withTitle(noteTitle)).perform(click())
        onView(withId(R.id.textField)).check(matches(allOf(isDisplayed(), withText(noteContent))))
        Espresso.pressBack()

        onData(withTitle(noteTitle)).perform(longClick())
        activity.clickMenuItem(activity.getString(R.string.optionDelete))
        onView(withText(activity.getString(R.string.deleteConfirmationMessage))).check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())
        assertToastPresent(R.string.deleteFeedback)

        onView(allOf(withId(R.id.title), withText(noteTitle))).check(doesNotExist())
    }
}
