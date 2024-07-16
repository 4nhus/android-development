package org.hyperskill.ordersmenu.internals

import android.app.Activity
import org.junit.Assert
import org.robolectric.shadows.ShadowToast

abstract class AbstractUnitTest<T : Activity>(clazz: Class<T>) {

    /**
     * Asserts that the last message toasted is the expectedMessage.
     * Assertion fails if no toast is shown with null actualLastMessage value.
     */
    fun assertLastToastMessageEquals(
        errorMessage: String,
        expectedMessage: String,
        clearToasts: Boolean = true
    ) {
        val actualLastMessage: String? = ShadowToast.getTextOfLatestToast()
        Assert.assertEquals(errorMessage, expectedMessage, actualLastMessage)

        if (clearToasts) {
            ShadowToast.reset()
        }
    }
}