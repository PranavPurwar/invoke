package org.invokevirtual.launcher.system

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenLockTest {

    private fun action(
        enabled: Boolean = true,
        supported: Boolean = true,
        excluded: Boolean = false,
        isEditMode: Boolean = false,
        serviceConnected: Boolean = true,
    ) = doubleTapAction(
        enabled = enabled,
        supported = supported,
        excluded = excluded,
        isEditMode = isEditMode,
        serviceConnected = serviceConnected,
    )

    @Test
    fun `does nothing when the setting is off`() {
        assertEquals(DoubleTapAction.NONE, action(enabled = false))
    }

    @Test
    fun `does nothing below Android 9 even with the setting restored on`() {
        assertEquals(DoubleTapAction.NONE, action(supported = false, serviceConnected = false))
    }

    @Test
    fun `does nothing on the dock or a widget`() {
        assertEquals(DoubleTapAction.NONE, action(excluded = true))
    }

    @Test
    fun `does nothing in edit mode`() {
        assertEquals(DoubleTapAction.NONE, action(isEditMode = true))
    }

    @Test
    fun `locks when the service is connected`() {
        assertEquals(DoubleTapAction.LOCK, action())
    }

    @Test
    fun `opens settings when the service is not connected`() {
        assertEquals(DoubleTapAction.OPEN_SETTINGS, action(serviceConnected = false))
    }
}
