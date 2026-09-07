package com.mcasillas.dragline.challenge

import com.mcasillas.dragline.domain.model.WakeChallengeType

sealed interface WakeChallengeState {
    data object Idle : WakeChallengeState
    data class InProgress(val progress: Float) : WakeChallengeState // 0.0f to 1.0f
    data object Completed : WakeChallengeState
    data class Failed(val reason: String) : WakeChallengeState
}

interface WakeChallenge {
    val type: WakeChallengeType
    val title: String
    val description: String
}
