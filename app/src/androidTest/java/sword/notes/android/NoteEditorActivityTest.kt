package sword.notes.android

import android.content.Context
import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.pressBack
import android.support.test.espresso.NoMatchingViewException
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.allOf
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

    @Test
    fun createNote() {
        val activity = activityTestRule.activity
        activity.clickMenuItem(activity.getString(R.string.optionNew))

        val noteTitle = "My test note"
        val noteContent = "This is my note content"

        onView(withText(activity.getString(R.string.createDialogTitle))).check(matches(isDisplayed()))
        onView(withId(R.id.textField)).perform(replaceText(noteTitle))
        onView(withText(activity.getString(android.R.string.yes))).check(matches(isDisplayed())).perform(click())

        onView(allOf(withId(R.id.title), withText(noteTitle))).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.textField)).check(matches(isDisplayed())).perform(replaceText(noteContent))
        activity.clickMenuItem(activity.getString(R.string.optionSave))
        pressBack()

        onView(allOf(withId(R.id.title), withText(noteTitle))).check(matches(isDisplayed())).perform(click())
        onView(withId(R.id.textField)).check(matches(allOf(isDisplayed(), withText(noteContent))))
        pressBack()

        onView(allOf(withId(R.id.title), withText(noteTitle))).check(matches(isDisplayed())).perform(longClick())
        activity.clickMenuItem(activity.getString(R.string.optionDelete))
        onView(withText(activity.getString(R.string.deleteConfirmationMessage))).check(matches(isDisplayed()))
        onView(withId(android.R.id.button1)).perform(click())

        onView(allOf(withId(R.id.title), withText(noteTitle))).check(doesNotExist())
    }
}
