package org.hyperskill.ordersmenu.internals

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import org.hyperskill.ordersmenu.MainActivity
import org.junit.Rule
import kotlin.math.abs

open class OrdersMenuUnitTest<T : Activity>(clazz: Class<T>) : AbstractUnitTest<T>(clazz) {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    val recipesOnMenuToInitialStockMap = mapOf(
        "Fettuccine" to 5,
        "Risotto" to 6,
        "Gnocchi" to 4,
        "Spaghetti" to 3,
        "Lasagna" to 5,
        "Steak Parmigiana" to 2
    )

    fun SemanticsNodeInteraction.assertTextStyle(block: (style: TextStyle) -> Unit) {
        val semanticsNode = fetchSemanticsNode()
        val config = semanticsNode.config
        val list = mutableListOf<TextLayoutResult>()
        config[SemanticsActions.GetTextLayoutResult].action?.invoke(list) // populates list
        block.invoke(list[0].layoutInput.style)
    }

    fun isTextHorizontallyCenteredOnWindow(toleranceMargin: Float = 10f): SemanticsMatcher {
        return SemanticsMatcher("is text horizontally centered on window") { node ->
            val rootNode = composeTestRule.onRoot().fetchSemanticsNode()
            val rootHorizontalCenter = rootNode.boundsInWindow.center.x

            val config = node.config
            val list = mutableListOf<TextLayoutResult>()

            val getTextLayoutResult = config.getOrNull(SemanticsActions.GetTextLayoutResult)
                ?: throw IllegalArgumentException("isTextCenteredOnWindow expects a Text node")

            getTextLayoutResult.action?.invoke(list) // populates list
            val textLayoutResult = list.getOrNull(0)
                ?: throw IllegalStateException("It was expected for a Text node to have TextLayoutResult")

            val textLeft =
                (0 until textLayoutResult.lineCount).fold(Float.POSITIVE_INFINITY) { acc, cur ->
                    val lineStart = textLayoutResult.getLineLeft(cur)
                    if (lineStart < acc) lineStart else acc
                } // value relative to node

            val textRight =
                (0 until textLayoutResult.lineCount).fold(Float.NEGATIVE_INFINITY) { acc, cur ->
                    val lineEnd = textLayoutResult.getLineRight(cur)
                    if (lineEnd > acc) lineEnd else acc
                } // value relative to node

            val textDiff = textRight - textLeft // assuming right is greater or equal than left
            val textHorizontalCenter = node.boundsInWindow.left + textLeft + (textDiff / 2.0f)

            abs(textHorizontalCenter - rootHorizontalCenter) < toleranceMargin
        }
    }

    fun isTextOnWindowStart(toleranceMargin: Float = 15f): SemanticsMatcher {
        return SemanticsMatcher("is text on window start") { node ->
            val root = composeTestRule.onRoot().fetchSemanticsNode()

            val rootStart = if (node.layoutInfo.layoutDirection == LayoutDirection.Rtl) {
                root.boundsInWindow.right
            } else {
                root.boundsInWindow.left
            }

            val config = node.config
            val list = mutableListOf<TextLayoutResult>()
            val getTextLayoutResult = config.getOrNull(SemanticsActions.GetTextLayoutResult)
                ?: throw IllegalArgumentException("isTextOnWindowStart expects a Text node")
            getTextLayoutResult.action?.invoke(list) // populates list
            val textLayoutResult = list.getOrNull(0)
                ?: throw IllegalStateException("It was expected for a Text node to have TextLayoutResult")

            val textStart = if (node.layoutInfo.layoutDirection == LayoutDirection.Rtl) {
                val textRight =
                    (0 until textLayoutResult.lineCount).fold(Float.NEGATIVE_INFINITY) { acc, cur ->
                        val lineEnd = textLayoutResult.getLineRight(cur)
                        if (lineEnd > acc) lineEnd else acc
                    } // value relative to node
                val offset = node.boundsInWindow.width - textRight
                node.boundsInWindow.right - offset
            } else {
                val textLeft =
                    (0 until textLayoutResult.lineCount).fold(Float.POSITIVE_INFINITY) { acc, cur ->
                        val lineStart = textLayoutResult.getLineLeft(cur)
                        if (lineStart < acc) lineStart else acc
                    } // value relative to node
                node.boundsInWindow.left + textLeft
            }

            abs(rootStart - textStart) < toleranceMargin
        }
    }

    fun isOnSameRowAs(otherNode: SemanticsNodeInteraction): SemanticsMatcher {
        return SemanticsMatcher(
            "is on same row as ${otherNode.nodeAsString()}"
        ) { node ->
            val otherNodeYPosition = otherNode.fetchSemanticsNode().positionInWindow.y
            val nodeYPosition = node.positionInWindow.y
            abs(otherNodeYPosition - nodeYPosition) < 10f
        }
    }

    fun isBelow(otherNode: SemanticsNodeInteraction): SemanticsMatcher {
        return SemanticsMatcher(
            "is bellow ${otherNode.nodeAsString()}"
        ) { node ->
            val nodeBellowY = node.layoutInfo.coordinates.boundsInWindow().top
            val nodeAboveY =
                otherNode.fetchSemanticsNode().layoutInfo.coordinates.boundsInWindow().bottom
            nodeBellowY >= nodeAboveY
        }
    }

    fun isButton(): SemanticsMatcher {
        return SemanticsMatcher(
            "has role Role.Button"
        ) { node ->
            val config = node.config
            val role = config.getOrNull(SemanticsProperties.Role)
            role == Role.Button
        }
    }

    fun assertNotOverlapEachOthers(
        listOfNodes: List<SemanticsNode>
    ) {
        listOfNodes.forEachIndexed { i, node ->
            val subList = listOfNodes.drop(i + 1)
            subList.forEach { anotherNode ->

                val hasIntersection =
                    anotherNode.boundsInWindow.intersect(node.boundsInWindow).let {
                        // from intersect docstring
                        // 'If the two rectangles do not overlap,
                        // then the resulting Rect will have a negative width or height'
                        it.width > 0 && it.height > 0
                    }


                assert(!hasIntersection) {
                    val nodeText = node.config.getOrNull(SemanticsProperties.Text).toString()
                    val anotherNodeText =
                        anotherNode.config.getOrNull(SemanticsProperties.Text).toString()
                    "View with text $nodeText and bounds ${node.boundsInWindow}" +
                            " should not overlap " +
                            "view with text $anotherNodeText and bounds ${anotherNode.boundsInWindow}"
                }
            }
        }
    }

    fun Color.rgbEquals(other: Color): Boolean {
        return this.red == other.red && this.green == other.green && this.blue == other.blue
    }

    fun SemanticsNodeInteraction.nodeAsString(): String {
        return this.printToString()
            .substringAfter("\n")
            .replace('\n', ' ')
    }

    //////////////////////

    fun debugResearchPurposes(activity: Activity) {
        val rootView = activity.window.decorView.rootView
        println(rootView.hierarchyString())
        println()
        println(composeTestRule.onRoot(useUnmergedTree = true).printToString(Int.MAX_VALUE))
    }

    fun View.hierarchyString(tag: String = "| "): String {

        val cur = "$tag ${this::class.java}"
        return if (this is ViewGroup) {
            val childrenString = (0 until this.childCount).map {
                this.getChildAt(it)
            }.fold("") { a, child ->
                val childString = child.hierarchyString("$tag |--")
                "$a\n$childString"
            }
            "$cur$childrenString"
        } else {
            cur
        }
    }
}