package com.mcasillas.dragline.domain.model

enum class WakeChallengeType(
    val title: String,
    val description: String,
    val isImplemented: Boolean
) {
    HOLD_TO_WAKE(
        title = "Hold to Wake",
        description = "Press and hold the dragline tether continuously for 3 seconds to prove you are awake.",
        isImplemented = true
    ),
    NFC_TAG(
        title = "NFC Tag",
        description = "Scan an NFC tag attached across the room to verify you are out of bed.",
        isImplemented = false
    ),
    QR_CODE(
        title = "QR Code",
        description = "Scan a designated QR code in your kitchen or bathroom.",
        isImplemented = false
    ),
    STEP_COUNT(
        title = "Step Count",
        description = "Walk a set number of steps before the alarm audio will cease.",
        isImplemented = false
    )
}
