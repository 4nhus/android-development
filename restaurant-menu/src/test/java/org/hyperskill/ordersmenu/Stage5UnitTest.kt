package org.hyperskill.ordersmenu

import android.app.Activity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.onSiblings
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hyperskill.ordersmenu.internals.OrdersMenuUnitTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.shadows.ShadowToast

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class Stage5UnitTest : OrdersMenuUnitTest<MainActivity>(MainActivity::class.java) {


    @Test
    fun test00_checkMakeOrderButtonExists() {
        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                val makeOderButtonNode = onNode(
                    hasTextExactly("Make Order").and(isButton())
                )

                makeOderButtonNode.apply {
                    assertExists("There should be a Button with text \"Make Order\"")
                    assertIsDisplayed()
                    assertHasClickAction()
                    assertTextStyle { style ->
                        assertTrue(
                            "Make Order button background is expected to have Color.Black",
                            style.background.rgbEquals(Color.Black)

                        )
                        assertTrue(
                            "Make Order button text color is expected to have Color.White",
                            style.color.rgbEquals(Color.White)
                        )
                    }
                }
            }
        }
    }

    @Test
    fun test01_checkToastIsSentWhenFettuccineOderIsSubmitted() {

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {
                val fettuccineNode = onNode(hasTextExactly("Fettuccine"))
                val siblings = fettuccineNode.onSiblings().filter(isOnSameRowAs(fettuccineNode))
                val plusNode = siblings.filterToOne(hasText("+"))
                    .assertExists("Fettuccine should have a sibling with text \"+\"")
                    .assertIsDisplayed()

                plusNode.performClick()

                val makeOderButtonNode = onNode(
                    hasTextExactly("Make Order").and(isButton())
                ).assertExists("There should be a Button with text \"Make Order\"")
                    .assertIsDisplayed()
                    .assertHasClickAction()

                makeOderButtonNode.performClick()

                assertLastToastMessageEquals(
                    errorMessage = "After one Fettuccine is ordered expected Toast with exact message",
                    expectedMessage = "Ordered:\n" +
                            "==> Fettuccine: 1",
                    clearToasts = true
                )
            }
        }
    }

    @Test
    fun test02_checkToastIsNotSentWhenEveryAmountIsZero() {

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {

                val makeOderButtonNode = onNode(
                    hasTextExactly("Make Order").and(isButton())
                ).assertExists("There should be a Button with text \"Make Order\"")
                    .assertIsDisplayed()
                    .assertHasClickAction()

                ShadowToast.reset()
                makeOderButtonNode.performClick()

                val textOfLatestToast: String? = ShadowToast.getTextOfLatestToast()

                assertNull(
                    "There should be no Toast message " +
                            "if no item has amount greater than zero",
                    textOfLatestToast
                )
            }
        }
    }

    @Test
    fun test03_checkAfterSteakParmigianaOrderStockAmountDecreases() {
        // assumes the existence in recipesOnMenuToInitialStockMap of entry "Steak Parmigiana" to 2

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            val recipeName = "Steak Parmigiana"
            composeTestRule.apply {

                val parmigianaNode = onNode(hasTextExactly(recipeName))
                val siblings = parmigianaNode.onSiblings().filter(isOnSameRowAs(parmigianaNode))
                val plusNode = siblings.filterToOne(hasTextExactly("+"))
                    .assertExists("$recipeName should have a sibling with text \"+\"")
                    .assertIsDisplayed()

                plusNode.performClick()

                siblings.filterToOne(hasTextExactly("1"))
                    .assertExists(
                        "After $recipeName plus is clicked for the first time" +
                                " increase the oder amount to 1"
                    )

                parmigianaNode.assertTextStyle { style ->
                    val errorMessageRedText =
                        "If amount ordered has not reached the stockAmount of $recipeName " +
                                "show $recipeName in black color"
                    assertTrue(errorMessageRedText, style.color.rgbEquals(Color.Black))
                }

                plusNode.performClick()

                parmigianaNode.assertTextStyle { style ->
                    val errorMessageRedText =
                        "When the amount ordered has reached the stockAmount of $recipeName " +
                                "show $recipeName in red color"
                    assertTrue(errorMessageRedText, style.color.rgbEquals(Color.Red))
                }

                val minusNode = siblings.filterToOne(hasTextExactly("-"))
                    .assertExists("$recipeName should have a sibling with text \"-\"")
                    .assertIsDisplayed()

                minusNode.performClick()

                val makeOderButtonNode = onNode(
                    hasTextExactly("Make Order").and(isButton())
                ).assertExists("There should be a Button with text \"Make Order\"")
                    .assertIsDisplayed()
                    .assertHasClickAction()

                makeOderButtonNode.performClick()

                assertLastToastMessageEquals(
                    errorMessage = "After one $recipeName is ordered expected Toast with exact message",
                    expectedMessage = "Ordered:\n" +
                            "==> $recipeName: 1",
                    clearToasts = true
                )

                siblings.filterToOne(hasTextExactly("0"))
                    .assertExists("After $recipeName order reset order amount to 0")

                plusNode.performClick()

                siblings.filterToOne(hasTextExactly("1"))
                    .assertExists(
                        "After an order and $recipeName plus is clicked " +
                                "increase the oder amount to 1"
                    )

                parmigianaNode.assertTextStyle { style ->
                    val errorMessageRedText =
                        "After one $recipeName is ordered the stockAmount should be 1" +
                                "so if orderAmount is one show $recipeName in red color"
                    assertTrue(errorMessageRedText, style.color.rgbEquals(Color.Red))
                }

                plusNode.performClick()

                siblings.filterToOne(hasTextExactly("1"))
                    .assertExists(
                        "After one $recipeName is ordered the stockAmount should be 1" +
                                "so if orderAmount is one and plus is clicked the orderAmount should remain"
                    )
            }
        }
    }

    @Test
    fun test04_checkAfterMenuItemOrderStockAmountDecreasesWithIndividualOrders() {

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {

                recipesOnMenuToInitialStockMap.forEach { (recipeName, initialStockAmount) ->
                    val menuItemNode = onNode(hasTextExactly(recipeName))
                    val siblings = menuItemNode.onSiblings().filter(isOnSameRowAs(menuItemNode))
                    val plusNode = siblings.filterToOne(hasTextExactly("+"))
                        .assertExists("$recipeName should have a sibling with text \"+\"")
                        .assertIsDisplayed()

                    (1 until initialStockAmount).forEach {
                        plusNode.performClick()

                        siblings.filterToOne(hasTextExactly("$it"))
                            .assertExists(
                                "After $recipeName plus is clicked $it times" +
                                        " increase its oder amount to $it"
                            )

                        menuItemNode.assertTextStyle { style ->
                            val errorMessageRedText =
                                "If amount ordered has not reached the stockAmount of $recipeName " +
                                        "show $recipeName in black color"
                            assertTrue(errorMessageRedText, style.color.rgbEquals(Color.Black))
                        }
                    }

                    plusNode.performClick()

                    menuItemNode.assertTextStyle { style ->
                        val errorMessageRedText =
                            "When the amount ordered has reached the stockAmount of $recipeName " +
                                    "show $recipeName in red color"
                        assertTrue(errorMessageRedText, style.color.rgbEquals(Color.Red))
                    }

                    val minusNode = siblings.filterToOne(hasTextExactly("-"))
                        .assertExists("$recipeName should have a sibling with text \"-\"")
                        .assertIsDisplayed()

                    minusNode.performClick()

                    val makeOderButtonNode = onNode(
                        hasTextExactly("Make Order").and(isButton())
                    ).assertExists("There should be a Button with text \"Make Order\"")
                        .assertIsDisplayed()
                        .assertHasClickAction()

                    makeOderButtonNode.performClick()

                    assertLastToastMessageEquals(
                        errorMessage = "After one $recipeName is ordered Toast with exact message",
                        expectedMessage = "Ordered:\n" +
                                "==> $recipeName: ${initialStockAmount - 1}",
                        clearToasts = true
                    )

                    siblings.filterToOne(hasTextExactly("0"))
                        .assertExists("After $recipeName order reset order amount to 0")

                    plusNode.performClick()

                    siblings.filterToOne(hasTextExactly("1"))
                        .assertExists(
                            "After an order and $recipeName plus is clicked " +
                                    "increase the oder amount to 1"
                        )

                    menuItemNode.assertTextStyle { style ->
                        val errorMessageRedText =
                            "if $recipeName stockAmount is 1" +
                                    "then when orderAmount is also 1 show $recipeName in red color"
                        assertTrue(errorMessageRedText, style.color.rgbEquals(Color.Red))
                    }

                    plusNode.performClick()

                    siblings.filterToOne(hasTextExactly("1"))
                        .assertExists(
                            "if $recipeName stockAmount is 1" +
                                    "then when orderAmount is 1 and plus is clicked the orderAmount should remain"
                        )

                    minusNode.performClick()
                }
            }
        }
    }

    @Test
    fun test05_checkAfterMenuItemOrderStockAmountDecreasesWithOneCollectiveOrder() {

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {

                recipesOnMenuToInitialStockMap.forEach { (recipeName, initialStockAmount) ->
                    val menuItemNode = onNode(hasTextExactly(recipeName))
                    val siblings = menuItemNode.onSiblings().filter(isOnSameRowAs(menuItemNode))
                    val plusNode = siblings.filterToOne(hasTextExactly("+"))
                        .assertExists("$recipeName should have a sibling with text \"+\"")
                        .assertIsDisplayed()

                    (1 until initialStockAmount).forEach {
                        plusNode.performClick()

                        siblings.filterToOne(hasTextExactly("$it"))
                            .assertExists(
                                "After $recipeName plus is clicked $it times" +
                                        " increase its oder amount to $it"
                            )

                        menuItemNode.assertTextStyle { style ->
                            val errorMessageRedText =
                                "If amount ordered has not reached the stockAmount of $recipeName " +
                                        "show $recipeName in black color"
                            assertTrue(errorMessageRedText, style.color.rgbEquals(Color.Black))
                        }
                    }

                    plusNode.performClick()

                    menuItemNode.assertTextStyle { style ->
                        val errorMessageRedText =
                            "When the amount ordered has reached the stockAmount of $recipeName " +
                                    "show $recipeName in red color"
                        assertTrue(errorMessageRedText, style.color.rgbEquals(Color.Red))
                    }

                    val minusNode = siblings.filterToOne(hasTextExactly("-"))
                        .assertExists("$recipeName should have a sibling with text \"-\"")
                        .assertIsDisplayed()

                    minusNode.performClick()

                }

                val makeOderButtonNode = onNode(
                    hasTextExactly("Make Order").and(isButton())
                ).assertExists("There should be a Button with text \"Make Order\"")
                    .assertIsDisplayed()
                    .assertHasClickAction()

                makeOderButtonNode.performClick()

                val expectedToastMessage = "Ordered:\n" +
                        recipesOnMenuToInitialStockMap.map { (recipeName, initialStockAmount) ->
                            "==> $recipeName: ${initialStockAmount - 1}"
                        }.joinToString("\n")

                assertLastToastMessageEquals(
                    errorMessage = "After order with every menu item is ordered Toast with exact message",
                    expectedMessage = expectedToastMessage,
                    clearToasts = true
                )

                recipesOnMenuToInitialStockMap.forEach { (recipeName, _) ->
                    val menuItemNode = onNode(hasTextExactly(recipeName))
                    val siblings = menuItemNode.onSiblings().filter(isOnSameRowAs(menuItemNode))

                    siblings.filterToOne(hasTextExactly("0"))
                        .assertExists("After $recipeName order reset order amount to 0")

                }

                recipesOnMenuToInitialStockMap.forEach { (recipeName, _) ->
                    val menuItemNode = onNode(hasTextExactly(recipeName))
                    val siblings = menuItemNode.onSiblings().filter(isOnSameRowAs(menuItemNode))
                    val plusNode = siblings.filterToOne(hasTextExactly("+"))
                        .assertExists("$recipeName should have a sibling with text \"+\"")
                        .assertIsDisplayed()

                    plusNode.performClick()

                    siblings.filterToOne(hasTextExactly("1"))
                        .assertExists(
                            "After an order and $recipeName plus is clicked " +
                                    "increase the oder amount to 1"
                        )

                    menuItemNode.assertTextStyle { style ->
                        val errorMessageRedText =
                            "if $recipeName stockAmount is 1" +
                                    "then when orderAmount is also 1 show $recipeName in red color"
                        assertTrue(errorMessageRedText, style.color.rgbEquals(Color.Red))
                    }

                    plusNode.performClick()

                    siblings.filterToOne(hasTextExactly("1"))
                        .assertExists(
                            "if $recipeName stockAmount is 1" +
                                    "then when orderAmount is 1 and plus is clicked the orderAmount should remain"
                        )
                    val minusNode = siblings.filterToOne(hasTextExactly("-"))
                        .assertExists("$recipeName should have a sibling with text \"-\"")
                        .assertIsDisplayed()

                    minusNode.performClick()
                }
            }
        }
    }

    @Test
    fun test06_checkAllOutOrder() {

        composeTestRule.activityRule.scenario.onActivity { activity: Activity ->

            composeTestRule.apply {

                recipesOnMenuToInitialStockMap.forEach { (recipeName, initialStockAmount) ->
                    val menuItemNode = onNode(hasTextExactly(recipeName))
                    val siblings = menuItemNode.onSiblings().filter(isOnSameRowAs(menuItemNode))
                    val plusNode = siblings.filterToOne(hasTextExactly("+"))
                        .assertExists("$recipeName should have a sibling with text \"+\"")
                        .assertIsDisplayed()

                    (1 until initialStockAmount).forEach {
                        plusNode.performClick()

                        siblings.filterToOne(hasTextExactly("$it"))
                            .assertExists(
                                "After $recipeName plus is clicked $it times" +
                                        " increase its oder amount to $it"
                            )

                        menuItemNode.assertTextStyle { style ->
                            val errorMessageRedText =
                                "If amount ordered has not reached the stockAmount of $recipeName " +
                                        "show $recipeName in black color"
                            assertTrue(errorMessageRedText, style.color.rgbEquals(Color.Black))
                        }
                    }

                    plusNode.performClick()

                    menuItemNode.assertTextStyle { style ->
                        val errorMessageRedText =
                            "When the amount ordered has reached the stockAmount of $recipeName " +
                                    "show $recipeName in red color"
                        assertTrue(errorMessageRedText, style.color.rgbEquals(Color.Red))
                    }

                }

                val makeOderButtonNode = onNode(
                    hasTextExactly("Make Order").and(isButton())
                ).assertExists("There should be a Button with text \"Make Order\"")
                    .assertIsDisplayed()
                    .assertHasClickAction()

                makeOderButtonNode.performClick()

                val expectedToastMessage = "Ordered:\n" +
                        recipesOnMenuToInitialStockMap.map { (recipeName, initialStockAmount) ->
                            "==> $recipeName: $initialStockAmount"
                        }.joinToString("\n")

                assertLastToastMessageEquals(
                    errorMessage = "After order with every menu item is ordered Toast with exact message",
                    expectedMessage = expectedToastMessage,
                    clearToasts = true
                )

                recipesOnMenuToInitialStockMap.forEach { (recipeName, _) ->
                    val menuItemNode = onNode(hasTextExactly(recipeName))
                    val siblings = menuItemNode.onSiblings().filter(isOnSameRowAs(menuItemNode))

                    siblings.filterToOne(hasTextExactly("0"))
                        .assertExists("After $recipeName order reset order amount to 0")

                }

                recipesOnMenuToInitialStockMap.forEach { (recipeName, _) ->
                    val menuItemNode = onNode(hasTextExactly(recipeName))
                    val siblings = menuItemNode.onSiblings().filter(isOnSameRowAs(menuItemNode))
                    val plusNode = siblings.filterToOne(hasTextExactly("+"))
                        .assertExists("$recipeName should have a sibling with text \"+\"")
                        .assertIsDisplayed()

                    plusNode.performClick()

                    siblings.filterToOne(hasTextExactly("0"))
                        .assertExists(
                            "if $recipeName stockAmount is 0" +
                                    "then when orderAmount should always remain 0"
                        )

                    menuItemNode.assertTextStyle { style ->
                        val errorMessageRedText =
                            "When the stock is all out all recipes including $recipeName should be red"
                        assertTrue(errorMessageRedText, style.color.rgbEquals(Color.Red))
                    }
                }
            }
        }
    }
}