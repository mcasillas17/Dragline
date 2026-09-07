package com.mcasillas.dragline.challenge.impl

import com.mcasillas.dragline.challenge.WakeChallenge
import com.mcasillas.dragline.domain.model.WakeChallengeType

class NfcChallenge(
    val expectedTagId: String? = null
) : WakeChallenge {
    override val type: WakeChallengeType = WakeChallengeType.NFC_TAG
    override val title: String = type.title
    override val description: String = type.description
}

class QrChallenge(
    val expectedPayloadHash: String? = null
) : WakeChallenge {
    override val type: WakeChallengeType = WakeChallengeType.QR_CODE
    override val title: String = type.title
    override val description: String = type.description
}

class StepCountChallenge(
    val targetSteps: Int = 30
) : WakeChallenge {
    override val type: WakeChallengeType = WakeChallengeType.STEP_COUNT
    override val title: String = type.title
    override val description: String = type.description
}
