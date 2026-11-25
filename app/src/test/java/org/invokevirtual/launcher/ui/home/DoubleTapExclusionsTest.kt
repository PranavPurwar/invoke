package org.invokevirtual.launcher.ui.home

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoubleTapExclusionsTest {

    private val dock = Rect(left = 0f, top = 2200f, right = 1080f, bottom = 2400f)
    private val widget = Rect(left = 40f, top = 900f, right = 1040f, bottom = 1300f)

    @Test
    fun `a tap inside the dock or a widget is excluded`() {
        assertTrue(isExcludedFromDoubleTap(Offset(500f, 2300f), listOf(dock, widget)))
        assertTrue(isExcludedFromDoubleTap(Offset(100f, 1000f), listOf(dock, widget)))
    }

    @Test
    fun `a tap on empty space is not excluded`() {
        assertFalse(isExcludedFromDoubleTap(Offset(500f, 1800f), listOf(dock, widget)))
        assertFalse(isExcludedFromDoubleTap(Offset(500f, 1800f), emptyList()))
    }
}
