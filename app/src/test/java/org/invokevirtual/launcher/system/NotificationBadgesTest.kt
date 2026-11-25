package org.invokevirtual.launcher.system

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationBadgesTest {

    private fun candidate(
        packageName: String = "com.whatsapp",
        userSerial: Long = 0L,
        number: Int = 0,
        isGroupSummary: Boolean = false,
        isOngoing: Boolean = false,
        channelId: String? = "messages",
        canShowBadge: Boolean = true,
        hasContent: Boolean = true,
    ) = BadgeCandidate(
        packageName = packageName,
        userSerial = userSerial,
        number = number,
        isGroupSummary = isGroupSummary,
        isOngoing = isOngoing,
        channelId = channelId,
        canShowBadge = canShowBadge,
        hasContent = hasContent,
    )

    private val whatsapp = BadgeKey("com.whatsapp", 0L)

    @Test
    fun `counts max of one and number per notification`() {
        val counts = countBadges(listOf(candidate(number = 0), candidate(number = 1), candidate(number = 6)))
        assertEquals(mapOf(whatsapp to 8), counts)
    }

    @Test
    fun `skips group summaries`() {
        val counts = countBadges(
            listOf(candidate(number = 3, isGroupSummary = true), candidate(), candidate()),
        )
        assertEquals(mapOf(whatsapp to 2), counts)
    }

    @Test
    fun `skips ongoing notifications on the miscellaneous channel only`() {
        assertTrue(countBadges(listOf(candidate(isOngoing = true, channelId = "miscellaneous"))).isEmpty())
        assertEquals(
            mapOf(whatsapp to 1),
            countBadges(listOf(candidate(isOngoing = true, channelId = "music"))),
        )
    }

    @Test
    fun `skips notifications the ranking hides`() {
        assertTrue(countBadges(listOf(candidate(canShowBadge = false))).isEmpty())
    }

    @Test
    fun `skips notifications without title or text`() {
        assertTrue(countBadges(listOf(candidate(hasContent = false))).isEmpty())
    }

    @Test
    fun `keeps profiles apart`() {
        val counts = countBadges(listOf(candidate(userSerial = 0L), candidate(userSerial = 10L, number = 4)))
        assertEquals(mapOf(whatsapp to 1, BadgeKey("com.whatsapp", 10L) to 4), counts)
    }

    @Test
    fun `folder total counts each package once`() {
        val counts = mapOf(BadgeKey("a", 0L) to 2, BadgeKey("b", 0L) to 5)
        val apps = listOf(
            AppKey("a", "a.Main", 0L),
            AppKey("a", "a.Other", 0L),
            AppKey("b", "b.Main", 0L),
            AppKey("c", "c.Main", 0L),
        )
        assertEquals(7, counts.totalFor(apps))
    }

    @Test
    fun `description uses singular for one notification`() {
        assertEquals("1 notification", badgeDescription(1))
        assertEquals("6 notifications", badgeDescription(6))
    }

    @Test
    fun `label caps at 99 plus`() {
        assertEquals("1", badgeLabel(1))
        assertEquals("99", badgeLabel(99))
        assertEquals("99+", badgeLabel(100))
    }
}
