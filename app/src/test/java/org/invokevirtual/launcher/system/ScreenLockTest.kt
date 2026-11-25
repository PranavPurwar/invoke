package org.invokevirtual.launcher.system

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenLockTest {

    @Test
    fun `does nothing when the setting is off`() {
        assertEquals(
            DoubleTapAction.NONE,
            doubleTapAction(enabled = false, isEditMode = false, serviceConnected = true),
        )
    }

    @Test
    fun `does nothing in edit mode`() {
        assertEquals(
            DoubleTapAction.NONE,
            doubleTapAction(enabled = true, isEditMode = true, serviceConnected = true),
        )
    }

    @Test
    fun `locks when the service is connected`() {
        assertEquals(
            DoubleTapAction.LOCK,
            doubleTapAction(enabled = true, isEditMode = false, serviceConnected = true),
        )
    }

    @Test
    fun `opens settings when the service is not connected`() {
        assertEquals(
            DoubleTapAction.OPEN_SETTINGS,
            doubleTapAction(enabled = true, isEditMode = false, serviceConnected = false),
        )
    }
}
