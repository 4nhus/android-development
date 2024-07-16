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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class Stage4UnitTest : OrdersMenuUnitTest<MainActivity>(MainActivity::class.java) {


    @Test
    fun test00_checkMenuItemAmountIncreases() {
        // assumes min value on recipesOnMenuToInitialStockMap is 2

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                recipesOnMenuToInitialStockMap.forEach { (recipeName, _) ->
                    val menuItemNode = onNodeWithText(
                        recipeName, substring = false, ignoreCase = false
                    )
                    val siblings = menuItemNode.onSiblings().filter(isOnSameRowAs(menuItemNode))
                    val plusNode = siblings.filterToOne(hasTextExactly("+"))
                        .assertExists("$recipeName should have a sibling with text \"+\"")
                        .assertIsDisplayed()

                    plusNode.performClick()
                    siblings.filterToOne(hasTextExactly("1"))
                        .assertExists(
                            "After clicking plus for the first time " +
                                    "the amount of $recipeName ordered should increase to 1"
                        )
                        .assertIsDisplayed()

                    plusNode.performClick()
                    siblings.filterToOne(hasTextExactly("2"))
                        .assertExists(
                            "After clicking plus for the second time " +
                                    "the amount of $recipeName ordered should increase to 2"
                        )
                        .assertIsDisplayed()
                }
            }
        }
    }

    @Test
    fun test01_checkMenuItemAmountIncreasesUpToLimitOnly() {

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                recipesOnMenuToInitialStockMap.forEach { (recipeName, stockQuantity) ->
                    val menuItemNode = onNodeWithText(
                        recipeName, substring = false, ignoreCase = false,
                    )
                    val siblings = menuItemNode.onSiblings().filter(isOnSameRowAs(menuItemNode))
                    val plusNode = siblings.filterToOne(hasTextExactly("+"))
                        .assertExists("$recipeName should have a sibling with text \"+\"")
                        .assertIsDisplayed()

                    (1..(stockQuantity * 2)).forEach {
                        plusNode.performClick()
                        val expectAmount = if (it <= stockQuantity) it else stockQuantity
                        siblings.filterToOne(hasTextExactly("$expectAmount"))
                            .assertExists(
                                "After clicking plus the amount of $recipeName ordered should increase " +
                                        "until stockQuantity is reached then it should remain"
                            )
                            .assertIsDisplayed()
                    }
                }
            }
        }
    }

    @Test
    fun test02_checkMenuItemWhenOnMaxLimitColorIsRed() {

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                recipesOnMenuToInitialStockMap.forEach { (recipeName, stockQuantity) ->
                    val menuItemNode = onNodeWithText(
                        recipeName, substring = false, ignoreCase = false
                    )

                    val siblings = menuItemNode.onSiblings().filter(isOnSameRowAs(menuItemNode))
                    val plusNode = siblings.filterToOne(hasTextExactly("+"))
                        .assertExists("$recipeName should have a sibling with text \"+\"")
                        .assertIsDisplayed()

                    (1..stockQuantity).forEach { _ ->
                        menuItemNode.assertTextStyle { style ->
                            assertTrue(
                                "While the amount ordered has not reached the stockQuantity of " +
                                        "$recipeName keep showing $recipeName in black color",
                                style.color.rgbEquals(Color.Black)
                            )
                        }
                        plusNode.performClick()
                    }

                    menuItemNode.assertExists().assertIsDisplayed()
                    menuItemNode.assertTextStyle { style ->
                        assertTrue(
                            "When the amount ordered has reached the stockQuantity of $recipeName " +
                                    "show $recipeName in red color",
                            style.color.rgbEquals(Color.Red)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun test03_checkMenuItemAmountDecrease() {
        // assumes min value on recipesOnMenuToInitialStockMap is 2

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                recipesOnMenuToInitialStockMap.forEach { (recipeName, stockQuantity) ->

                    val menuItemNode = onNodeWithText(
                        recipeName, substring = false, ignoreCase = false
                    )

                    val siblings = menuItemNode.onSiblings().filter(isOnSameRowAs(menuItemNode))
                    val plusNode = siblings.filterToOne(hasTextExactly("+"))
                        .assertExists("$recipeName should have a sibling with text \"+\"")
                        .assertIsDisplayed()

                    (1..(stockQuantity * 2)).forEach { _ ->
                        plusNode.performClick()
                    }

                    menuItemNode.assertExists()
                    menuItemNode.assertIsDisplayed()
                    siblings.filterToOne(hasTextExactly("$stockQuantity"))
                        .assertExists(
                            "After clicking plus more times than the stockQuantity the amount " +
                                    "of $recipeName ordered should be equal to the stockQuantity"
                        )
                        .assertIsDisplayed()

                    val minusNode = siblings.filterToOne(hasTextExactly("-"))
                        .assertExists("$recipeName should have a sibling with text \"-\"")
                        .assertIsDisplayed()


                    minusNode.performClick()
                    siblings.filterToOne(hasTextExactly("${stockQuantity - 1}"))
                        .assertExists(
                            "While the amount ordered is 5, after clicking minus " +
                                    "the amount of $recipeName ordered should decrease to 4"
                        )
                        .assertIsDisplayed()

                    minusNode.performClick()
                    siblings.filterToOne(hasTextExactly("${stockQuantity - 2}"))
                        .assertExists(
                            "While the amount ordered is 4, after clicking minus " +
                                    "the amount of $recipeName ordered should decrease to 3"
                        )
                        .assertIsDisplayed()
                }
            }
        }
    }

    @Test
    fun test04_checkAmountDecreaseDoesNotGoBellowZero() {

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                recipesOnMenuToInitialStockMap.forEach { (recipeName, stockQuantity) ->

                    val menuItemNode = onNodeWithText(
                        recipeName, substring = false, ignoreCase = false
                    )

                    val siblings = menuItemNode.onSiblings().filter(isOnSameRowAs(menuItemNode))
                    val minusNode = siblings.filterToOne(hasTextExactly("-"))
                        .assertExists("$recipeName should have a sibling with text \"-\"")
                        .assertIsDisplayed()

                    siblings.filterToOne(hasTextExactly("0"))
                        .assertExists("The initial amount of $recipeName ordered should be zero")
                        .assertIsDisplayed()

                    minusNode.performClick()
                    siblings.filterToOne(hasTextExactly("0"))
                        .assertExists(
                            "When the $recipeName ordered amount is zero " +
                                    "and minus is clicked the amount should remain zero"
                        )
                        .assertIsDisplayed()

                    val plusNode = siblings.filterToOne(hasTextExactly("+"))
                        .assertExists("$recipeName should have a sibling with text \"+\"")
                        .assertIsDisplayed()

                    (1..(stockQuantity * 2)).forEach { _ ->
                        plusNode.performClick()
                    }

                    menuItemNode.assertExists()
                    menuItemNode.assertIsDisplayed()
                    siblings.filterToOne(hasTextExactly("$stockQuantity"))
                        .assertExists(
                            "After clicking plus more times than the stockQuantity the amount" +
                                    "of $recipeName ordered should be equal to the stockQuantity"
                        )
                        .assertIsDisplayed()

                    (1..(stockQuantity * 2)).map { stockQuantity - it }.forEach { i ->
                        val expectAmount = if (i > 0) i else 0
                        minusNode.performClick()
                        siblings.filterToOne(hasTextExactly("$expectAmount"))
                            .assertExists(
                                "After clicking minus the amount of $recipeName ordered should decrease " +
                                        "until zero is reached then it should remain zero"
                            )
                            .assertIsDisplayed()
                    }
                }
            }
        }
    }

    @Test
    fun test05_whileColorIsRedCheckColorChangesBackToBlackAfterDecrement() {

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                recipesOnMenuToInitialStockMap.forEach { (recipeName, stockQuantity) ->

                    val menuItemNode = onNodeWithText(
                        recipeName, substring = false, ignoreCase = false
                    )

                    val siblings = menuItemNode.onSiblings().filter(isOnSameRowAs(menuItemNode))

                    val plusNode = siblings.filterToOne(hasTextExactly("+"))
                        .assertExists("$recipeName should have a sibling with text \"+\"")
                        .assertIsDisplayed()

                    (1..stockQuantity).forEach { _ ->
                        menuItemNode.assertTextStyle { style ->
                            assertTrue(
                                "While the amount ordered has not reached the stockQuantity of " +
                                        "$recipeName keep showing $recipeName in black color",
                                style.color.rgbEquals(Color.Black)
                            )
                        }
                        plusNode.performClick()
                    }

                    menuItemNode.assertExists().assertIsDisplayed()
                    menuItemNode.assertTextStyle { style ->
                        assertTrue(
                            "When the amount ordered has reached the quantity of $recipeName in stock " +
                                    "show $recipeName in red color",
                            style.color.rgbEquals(Color.Red)
                        )
                    }

                    val minusNode = siblings.filterToOne(hasTextExactly("-"))
                        .assertExists("$recipeName should have a sibling with text \"-\"")
                        .assertIsDisplayed()

                    minusNode.performClick()

                    menuItemNode.assertExists().assertIsDisplayed()
                    menuItemNode.assertTextStyle { style ->
                        assertEquals(
                            "When $recipeName color is red and minus is clicked " +
                                    "then $recipeName color should become black",
                            Color.Black,
                            style.color
                        )
                    }
                }
            }
        }
    }
}