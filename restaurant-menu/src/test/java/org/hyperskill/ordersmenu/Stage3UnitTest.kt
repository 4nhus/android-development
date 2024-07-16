package org.hyperskill.ordersmenu

import android.app.Activity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onSiblings
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hyperskill.ordersmenu.internals.OrdersMenuUnitTest
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class Stage3UnitTest : OrdersMenuUnitTest<MainActivity>(MainActivity::class.java) {


    @Test
    fun test00_checkFettuccineAmountIncreases() {
        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                val fettuccineNode = onNodeWithText(
                    "Fettuccine", substring = false, ignoreCase = false
                )
                val siblings = fettuccineNode.onSiblings().filter(isOnSameRowAs(fettuccineNode))
                val plusNode = siblings.filterToOne(hasTextExactly("+"))
                    .assertExists("Fettuccine should have a sibling with text \"+\"")
                    .assertIsDisplayed()

                plusNode.performClick()
                siblings.filterToOne(hasTextExactly("1"))
                    .assertExists(
                        "After clicking plus for the first time " +
                                "the amount of Fettuccine ordered should increase to 1"
                    )
                    .assertIsDisplayed()

                plusNode.performClick()
                siblings.filterToOne(hasTextExactly("2"))
                    .assertExists(
                        "After clicking plus for the second time " +
                                "the amount of Fettuccine ordered should increase to 2"
                    )
                    .assertIsDisplayed()
            }
        }
    }

    @Test
    fun test01_checkFettuccineAmountIncreasesUpToLimitOnly() {
        val limit = 5

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                val fettuccineNode = onNodeWithText(
                    "Fettuccine", substring = false, ignoreCase = false,
                )
                val siblings = fettuccineNode.onSiblings().filter(isOnSameRowAs(fettuccineNode))
                val plusNode = siblings.filterToOne(hasTextExactly("+"))
                    .assertExists("Fettuccine should have a sibling with text \"+\"")
                    .assertIsDisplayed()

                (1..10).forEach {
                    plusNode.performClick()
                    val expectAmount = if (it <= limit) it else limit
                    siblings.filterToOne(hasTextExactly("$expectAmount"))
                        .assertExists(
                            "After clicking plus the amount of Fettuccine ordered should increase " +
                                    "until limit is reached then it should remain"
                        )
                        .assertIsDisplayed()
                }
            }
        }
    }

    @Test
    fun test02_checkFettuccineWhenOnMaxLimitColorIsRed() {

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                val fettuccineNode = onNodeWithText(
                    "Fettuccine", substring = false, ignoreCase = false
                )
                fettuccineNode.assertTextStyle { style ->
                    assertTrue(
                        "While the amount ordered has not reached the quantity of " +
                                "Fettuccine in stock keep showing Fettuccine in black color",
                        style.color.rgbEquals(Color.Black)
                    )
                }
                val siblings = fettuccineNode.onSiblings().filter(isOnSameRowAs(fettuccineNode))
                val plusNode = siblings.filterToOne(hasTextExactly("+"))
                    .assertExists("Fettuccine should have a sibling with text \"+\"")
                    .assertIsDisplayed()

                (1..10).forEach { _ ->
                    plusNode.performClick()
                }

                fettuccineNode.assertExists().assertIsDisplayed()
                fettuccineNode.assertTextStyle { style ->
                    assertTrue(
                        "When the amount ordered has reached the quantity of fettuccine in stock " +
                                "show Fettuccine in red color",
                        style.color.rgbEquals(Color.Red)
                    )
                }
            }
        }
    }

    @Test
    fun test03_checkAmountDecrease() {

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                val fettuccineNode = onNodeWithText(
                    "Fettuccine", substring = false, ignoreCase = false
                )

                val siblings = fettuccineNode.onSiblings().filter(isOnSameRowAs(fettuccineNode))
                val plusNode = siblings.filterToOne(hasTextExactly("+"))
                    .assertExists("Fettuccine should have a sibling with text \"+\"")
                    .assertIsDisplayed()

                (1..10).forEach { _ ->
                    plusNode.performClick()
                }

                fettuccineNode.assertExists()
                fettuccineNode.assertIsDisplayed()
                siblings.filterToOne(hasTextExactly("5"))
                    .assertExists(
                        "After clicking plus more than the limit times the amount " +
                                "of Fettuccine ordered should be equal to the limit"
                    )
                    .assertIsDisplayed()

                val minusNode = siblings.filterToOne(hasTextExactly("-"))
                    .assertExists("Fettuccine should have a sibling with text \"-\"")
                    .assertIsDisplayed()


                minusNode.performClick()
                siblings.filterToOne(hasTextExactly("4"))
                    .assertExists(
                        "While the amount ordered is 5, after clicking minus " +
                                "the amount of Fettuccine ordered should decrease to 4"
                    )
                    .assertIsDisplayed()

                minusNode.performClick()
                siblings.filterToOne(hasTextExactly("3"))
                    .assertExists(
                        "While the amount ordered is 4, after clicking minus " +
                                "the amount of Fettuccine ordered should decrease to 3"
                    )
                    .assertIsDisplayed()
            }
        }
    }

    @Test
    fun test04_checkAmountDecreaseDoesNotGoBellowZero() {

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                val fettuccineNode = onNodeWithText(
                    "Fettuccine", substring = false, ignoreCase = false
                )

                val siblings = fettuccineNode.onSiblings().filter(isOnSameRowAs(fettuccineNode))
                val minusNode = siblings.filterToOne(hasTextExactly("-"))
                    .assertExists("Fettuccine should have a sibling with text \"-\"")
                    .assertIsDisplayed()

                siblings.filterToOne(hasTextExactly("0"))
                    .assertExists("The initial amount of Fettuccine ordered should be zero")
                    .assertIsDisplayed()

                minusNode.performClick()
                siblings.filterToOne(hasTextExactly("0"))
                    .assertExists(
                        "When the Fettuccine ordered amount is zero " +
                                "and minus is clicked the amount should remain zero"
                    )
                    .assertIsDisplayed()

                val plusNode = siblings.filterToOne(hasTextExactly("+"))
                    .assertExists("Fettuccine should have a sibling with text \"+\"")
                    .assertIsDisplayed()

                (1..10).forEach { _ ->
                    plusNode.performClick()
                }

                fettuccineNode.assertExists()
                fettuccineNode.assertIsDisplayed()
                siblings.filterToOne(hasTextExactly("5"))
                    .assertExists(
                        "After clicking plus more than the limit times the amount " +
                                "of Fettuccine ordered should be equal to the limit"
                    )
                    .assertIsDisplayed()

                (1..10).map { 5 - it }.forEach { i ->
                    val expectAmount = if (i > 0) i else 0
                    minusNode.performClick()
                    siblings.filterToOne(hasTextExactly("$expectAmount"))
                        .assertExists(
                            "After clicking minus the amount of Fettuccine ordered should decrease " +
                                    "until zero is reached then it should remain"
                        )
                        .assertIsDisplayed()
                }
            }
        }
    }

    @Test
    fun test05_whileColorIsRedCheckColorChangesBackToBlackAfterDecrement() {

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                val fettuccineNode = onNodeWithText(
                    "Fettuccine", substring = false, ignoreCase = false
                )

                fettuccineNode.assertTextStyle { style ->
                    assertTrue(
                        "While the amount ordered has not reached the quantity of " +
                                "Fettuccine in stock keep showing Fettuccine in black color",
                        style.color.rgbEquals(Color.Black)
                    )
                }
                val siblings = fettuccineNode.onSiblings().filter(isOnSameRowAs(fettuccineNode))

                val plusNode = siblings.filterToOne(hasTextExactly("+"))
                    .assertExists("Fettuccine should have a sibling with text \"+\"")
                    .assertIsDisplayed()

                (1..10).forEach { _ ->
                    plusNode.performClick()
                }

                fettuccineNode.assertExists().assertIsDisplayed()
                fettuccineNode.assertTextStyle { style ->
                    assertTrue(
                        "When the amount ordered has reached the quantity of fettuccine in stock " +
                                "show Fettuccine in red color",
                        style.color.rgbEquals(Color.Red)
                    )
                }


                val minusNode = siblings.filterToOne(hasTextExactly("-"))
                    .assertExists("Fettuccine should have a sibling with text \"-\"")
                    .assertIsDisplayed()


                minusNode.performClick()


                fettuccineNode.assertExists().assertIsDisplayed()
                fettuccineNode.assertTextStyle { style ->
                    assertTrue(
                        "When Fettuccine color is red and minus is clicked " +
                                "then Fettuccine color should become black",
                        style.color.rgbEquals(Color.Black)
                    )
                }
            }
        }
    }
}