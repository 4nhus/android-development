package org.hyperskill.ordersmenu

import android.app.Activity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTextExactly
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hyperskill.ordersmenu.internals.OrdersMenuUnitTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Stage1UnitTest : OrdersMenuUnitTest<MainActivity>(MainActivity::class.java) {


    @Test
    fun checkTitle() {

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                val titleNode = onNode(hasTextExactly("Orders Menu"))
                titleNode.assertExists("There should exist a title node with text \"Orders Menu\"")
                titleNode.assertIsDisplayed()

            }
        }
    }
}