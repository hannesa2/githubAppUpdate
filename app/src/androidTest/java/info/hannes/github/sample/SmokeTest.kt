package info.hannes.github.sample

import androidx.test.core.graphics.writeToTestStorage
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.screenshot.captureToBitmap
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SmokeTest {

    @get:Rule
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @get:Rule
    var nameRule = TestName()

    @Test
    fun smokeTestSimplyStart() {
        onView(isRoot())
            .captureToBitmap()
            .writeToTestStorage("${javaClass.simpleName}_${nameRule.methodName}-R")

        onView(withId(R.id.button)).perform(click())
        onView(isRoot())
            .captureToBitmap()
            .writeToTestStorage("${javaClass.simpleName}_${nameRule.methodName}-2")
        onView(withText(info.hannes.github.R.string.new_version))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(isRoot())
            .captureToBitmap()
            .writeToTestStorage("${javaClass.simpleName}_${nameRule.methodName}-3")
    }

    @Test
    fun updateGithub() {
        onView(isRoot())
            .captureToBitmap()
            .writeToTestStorage("${javaClass.simpleName}_${nameRule.methodName}-1")
        onView(withId(R.id.button)).perform(click())
        onView(isRoot())
            .captureToBitmap()
            .writeToTestStorage("${javaClass.simpleName}_${nameRule.methodName}-2")
        onView(withText(info.hannes.github.R.string.new_version))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
        onView(isRoot())
            .captureToBitmap()
            .writeToTestStorage("${javaClass.simpleName}_${nameRule.methodName}-3")

//        takeScreenshot()
//            .writeToTestStorage("${javaClass.simpleName}_${nameRule.methodName}-all")

        onView(withText("SHOW"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())
    }
}
