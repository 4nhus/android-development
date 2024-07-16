package org.hyperskill.ordersmenu

import android.app.Activity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.onSiblings
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hyperskill.ordersmenu.internals.OrdersMenuUnitTest
import org.junit.Assert.assertEquals
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class Stage2UnitTest : OrdersMenuUnitTest<MainActivity>(MainActivity::class.java) {


    @Test
    fun test00_checkTitle() {
        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {

                onNode(hasTextExactly("Orders Menu")).apply {
                    assertExists("There should exist a title node with text \"Orders Menu\"")
                    assertIsDisplayed()

                    assertTextStyle { style ->
                        assertEquals("The title should have size 48sp", 48.sp, style.fontSize)
                    }

                    assert(isTextHorizontallyCenteredOnWindow()) { "Title should be centered" }
                }

            }
        }
    }

    @Test
    fun test01_checkFettuccine() {
        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {

                onNode(hasTextExactly("Fettuccine")).apply {
                    assertExists("There should exist a title node with text \"Fettuccine\"")
                    assertIsDisplayed()

                    assertTextStyle { style ->
                        assertEquals("Fettuccine should have size 24sp", 24.sp, style.fontSize)
                    }

                    assert(isTextOnWindowStart()) { "Fettuccine should be displayed at start" }
                }
            }
        }
    }

    @Test
    fun test02_checkTitleIsAboveFettuccine() {
        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {

                val title = onNode(hasTextExactly("Orders Menu"))

                onNode(hasTextExactly("Fettuccine")).apply {
                    assert(isBelow(title))
                }
            }
        }
    }

    @Test
    fun test03_checkOverlap() {
        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                val titleNode = onNode(hasTextExactly("Orders Menu"))
                val siblingsTitle = titleNode.onSiblings().fetchSemanticsNodes(
                    atLeastOneRootRequired = true
                )
                val allNodes = siblingsTitle + titleNode.fetchSemanticsNode()
                assertNotOverlapEachOthers(allNodes)
            }
        }
    }
}