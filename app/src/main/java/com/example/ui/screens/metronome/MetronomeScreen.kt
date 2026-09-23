package com.example.ui.screens.metronome

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetronomeScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var bpm by remember { mutableIntStateOf(100) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentBeat by remember { mutableIntStateOf(0) }
    var timeSignature by remember { mutableIntStateOf(4) } // 4/4 default

    // Tap tempo timestamps
    val tapTimes = remember { mutableStateListOf<Long>() }

    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            null
        }
    }

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    // Metronome tick loop
    LaunchedEffect(isPlaying, bpm, timeSignature) {
        if (isPlaying) {
            val intervalMs = (60000L / bpm).coerceAtLeast(100L)
            while (isActive) {
                // Play sound
                try {
                    val isFirstBeat = (currentBeat % timeSignature) == 0
                    if (isFirstBeat) {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 40)
                    } else {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 25)
                    }
                } catch (_: Exception) {}

                // Haptic feedback
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(20L, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(20L)
                    }
                } catch (_: Exception) {}

                delay(intervalMs)
                currentBeat = (currentBeat + 1) % timeSignature
            }
        } else {
            currentBeat = 0
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                toneGenerator?.release()
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.metronome_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Visual Beat Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until timeSignature) {
                    val isCurrent = isPlaying && (currentBeat == i)
                    val isFirst = i == 0
                    Box(
                        modifier = Modifier
                            .size(if (isFirst) 28.dp else 22.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCurrent) {
                                    if (isFirst) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                    )
                }
            }

            // Central BPM readout
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$bpm",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "BPM",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // BPM Slider & Quick Increment Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Slider(
                    value = bpm.toFloat(),
                    onValueChange = { bpm = it.toInt() },
                    valueRange = 40f..240f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(onClick = { bpm = (bpm - 5).coerceAtLeast(40) }) {
                        Text("-5")
                    }
                    OutlinedButton(onClick = { bpm = (bpm - 1).coerceAtLeast(40) }) {
                        Text("-1")
                    }
                    OutlinedButton(onClick = { bpm = (bpm + 1).coerceAtMost(240) }) {
                        Text("+1")
                    }
                    OutlinedButton(onClick = { bpm = (bpm + 5).coerceAtMost(240) }) {
                        Text("+5")
                    }
                }
            }

            // Time Signature selector & Tap Tempo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time signatures
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(2 to "2/4", 3 to "3/4", 4 to "4/4", 6 to "6/8").forEach { (sig, label) ->
                        FilterChip(
                            selected = timeSignature == sig,
                            onClick = { timeSignature = sig },
                            label = { Text(label) }
                        )
                    }
                }

                // Tap Tempo button
                Button(
                    onClick = {
                        val now = System.currentTimeMillis()
                        tapTimes.add(now)
                        if (tapTimes.size > 5) tapTimes.removeAt(0)
                        if (tapTimes.size >= 2) {
                            val intervals = mutableListOf<Long>()
                            for (j in 1 until tapTimes.size) {
                                intervals.add(tapTimes[j] - tapTimes[j - 1])
                            }
                            val avgInterval = intervals.average()
                            if (avgInterval in 250.0..1500.0) {
                                bpm = (60000.0 / avgInterval).toInt().coerceIn(40, 240)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.metronome_tap_tempo))
                }
            }

            // Play / Stop main button
            StudioButton(
                text = if (isPlaying) stringResource(R.string.metronome_stop) else stringResource(R.string.metronome_start),
                onClick = { isPlaying = !isPlaying },
                icon = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                testTag = "metronome_toggle_button"
            )
        }
    }
}
