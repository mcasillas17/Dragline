package com.mcasillas.dragline.challenge.impl

import com.mcasillas.dragline.challenge.WakeChallenge
import com.mcasillas.dragline.domain.model.WakeChallengeType

class HoldToWakeChallenge(
    val requiredDurationMs: Long = 3000L
) : WakeChallenge {
    override val type: WakeChallengeType = WakeChallengeType.HOLD_TO_WAKE
    override val title: String = type.title
    override val description: String = type.description

    fun calculateProgress(heldDurationMs: Long): Float {
        if (heldDurationMs <= 0L) return 0f
        return (heldDurationMs.toFloat() / requiredDurationMs).coerceIn(0f, 1f)
    }

    fun isComplete(heldDurationMs: Long): Boolean {
        return heldDurationMs >= requiredDurationMs
    }
}
