package com.mcasillas.dragline.ui.ringing

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mcasillas.dragline.BuildConfig
import com.mcasillas.dragline.domain.model.WakeChallengeType
import com.mcasillas.dragline.ui.common.TetherDivider
import com.mcasillas.dragline.ui.theme.DarkBackground
import com.mcasillas.dragline.ui.theme.DraglineTheme
import com.mcasillas.dragline.ui.theme.StatusSuccess
import com.mcasillas.dragline.ui.theme.TetherAmber
import com.mcasillas.dragline.ui.theme.TetherCyan

@Composable
fun RingingScreen(
    viewModel: RingingViewModel,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onFinished()
        }
    }

    RingingScreenContent(
        uiState = uiState,
        onHoldStarted = { viewModel.onHoldStarted() },
        onHoldReleased = { viewModel.onHoldReleased() },
        onDebugDismiss = {
            viewModel.debugDismiss()
            onFinished()
        },
        modifier = modifier
    )
}

@Composable
fun RingingScreenContent(
    uiState: RingingUiState,
    onHoldStarted: () -> Unit,
    onHoldReleased: () -> Unit,
    onDebugDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Time & Label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Text(
                    text = uiState.currentTime.ifBlank { "07:00:00" },
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = uiState.alarmLabel.ifBlank { "Alarm" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = TetherAmber,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Active Audio Source Badge
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = uiState.soundSourceDisplayName,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = TetherCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                TetherDivider(modifier = Modifier.padding(horizontal = 32.dp))
            }

            // Middle Section: Hold To Wake Challenge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "HOLD TO WAKE",
                    style = MaterialTheme.typography.titleMedium,
                    color = TetherCyan,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Press and hold the tether for 3 seconds\nto prove you are awake.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Interactive Hold Button with Circular Progress Arc
                HoldToWakeButton(
                    progress = uiState.holdProgress,
                    isHolding = uiState.isHolding,
                    isCompleted = uiState.isCompleted,
                    onHoldStart = onHoldStarted,
                    onHoldEnd = onHoldReleased
                )
            }

            // Bottom Section: Debug Escape (only in debug builds)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                if (BuildConfig.DEBUG) {
                    OutlinedButton(
                        onClick = onDebugDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.outline
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Debug Dismiss (Dev Only)",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HoldToWakeButton(
    progress: Float,
    isHolding: Boolean,
    isCompleted: Boolean,
    onHoldStart: () -> Unit,
    onHoldEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "HoldProgress"
    )

    val buttonColor by animateColorAsState(
        targetValue = when {
            isCompleted -> StatusSuccess
            isHolding -> TetherAmber
            else -> TetherAmber.copy(alpha = 0.2f)
        },
        label = "ButtonColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(160.dp)
    ) {
        // Progress Ring Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f

            // Track circle
            drawCircle(
                color = Color.DarkGray.copy(alpha = 0.5f),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Dynamic progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    color = if (isCompleted) StatusSuccess else TetherCyan,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Central touch circle
        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(buttonColor)
                .pointerInput(isCompleted) {
                    if (!isCompleted) {
                        detectTapGestures(
                            onPress = {
                                onHoldStart()
                                val released = try {
                                    tryAwaitRelease()
                                } catch (e: Exception) {
                                    false
                                }
                                onHoldEnd()
                            }
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.TouchApp,
                    contentDescription = "Hold tether to silence alarm",
                    tint = if (isHolding || isCompleted) Color.Black else TetherAmber,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isCompleted) "Awake!" else if (isHolding) "Hold..." else "Hold",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isHolding || isCompleted) Color.Black else Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0F12)
@Composable
fun RingingScreenPreview() {
    DraglineTheme {
        RingingScreenContent(
            uiState = RingingUiState(
                currentTime = "07:00:12 AM",
                alarmLabel = "Morning Indie Rise",
                soundSourceDisplayName = "Morning Indie (Local Fallback)",
                isFallbackActive = true,
                wakeChallengeType = WakeChallengeType.HOLD_TO_WAKE,
                holdProgress = 0.45f,
                isHolding = true,
                isCompleted = false
            ),
            onHoldStarted = {},
            onHoldReleased = {},
            onDebugDismiss = {}
        )
    }
}
