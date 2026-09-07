package com.mcasillas.dragline.challenge

import com.mcasillas.dragline.challenge.impl.HoldToWakeChallenge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HoldToWakeChallengeTest {

    private lateinit var challenge: HoldToWakeChallenge

    @Before
    fun setup() {
        challenge = HoldToWakeChallenge(requiredDurationMs = 3000L)
    }

    @Test
    fun `progress calculation handles edge cases`() {
        assertEquals(0f, challenge.calculateProgress(0L), 0.001f)
        assertEquals(0f, challenge.calculateProgress(-500L), 0.001f)
        assertEquals(0.5f, challenge.calculateProgress(1500L), 0.001f)
        assertEquals(1.0f, challenge.calculateProgress(3000L), 0.001f)
        assertEquals(1.0f, challenge.calculateProgress(4000L), 0.001f)
    }

    @Test
    fun `isComplete returns true only when duration reached`() {
        assertFalse(challenge.isComplete(0L))
        assertFalse(challenge.isComplete(2999L))
        assertTrue(challenge.isComplete(3000L))
        assertTrue(challenge.isComplete(4500L))
    }
}
