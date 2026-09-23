package com.example.ui.screens.voice

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.core.content.ContextCompat
import com.example.LyricStudioApp
import com.example.R
import com.example.data.local.entity.VoiceMemoEntity
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioConfirmDialog
import com.example.ui.components.StudioEmptyState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = LyricStudioApp.instance
    val repo = app.voiceRepository
    val scope = rememberCoroutineScope()

    val memos by repo.allVoiceMemos.collectAsState(initial = emptyList())

    var isRecording by remember { mutableStateOf(false) }
    var recordingDurationSeconds by remember { mutableIntStateOf(0) }
    var currentPlayingId by remember { mutableStateOf<Long?>(null) }

    var memoToDelete by remember { mutableStateOf<VoiceMemoEntity?>(null) }
    var memoToRename by remember { mutableStateOf<VoiceMemoEntity?>(null) }
    var renameText by remember { mutableStateOf("") }

    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentOutputFile by remember { mutableStateOf<File?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "دسترسی میکروفون با موفقیت تأیید شد", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "دسترسی به میکروفون برای ضبط صدا الزامی است", Toast.LENGTH_LONG).show()
        }
    }

    // Recording duration timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDurationSeconds = 0
            while (isRecording) {
                delay(1000L)
                recordingDurationSeconds++
            }
        }
    }

    fun startRecording() {
        val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!hasPerm) {
            hasAudioPermission = false
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        hasAudioPermission = true

        try {
            try {
                mediaRecorder?.reset()
                mediaRecorder?.release()
            } catch (_: Exception) {}
            mediaRecorder = null

            val dir = File(context.filesDir, "voice_memos")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "memo_${System.currentTimeMillis()}.m4a")
            currentOutputFile = file

            val recorder = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }
            } catch (_: Exception) {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            isRecording = true
            recordingDurationSeconds = 0
            Toast.makeText(context, "ضبط صدا آغاز شد...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
            try {
                mediaRecorder?.release()
            } catch (_: Exception) {}
            mediaRecorder = null
            Toast.makeText(context, "خطا در شروع ضبط صدا: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun stopRecording() {
        val recorder = mediaRecorder
        val file = currentOutputFile
        val durationSecs = recordingDurationSeconds

        try {
            recorder?.let {
                try {
                    it.stop()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        it.reset()
                        it.release()
                    } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
            isRecording = false
        }

        if (file != null && file.exists() && file.length() > 0) {
            val now = System.currentTimeMillis()
            val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(now))
            val finalDuration = if (durationSecs > 0) durationSecs * 1000L else 1000L
            scope.launch {
                repo.saveVoiceMemo(
                    VoiceMemoEntity(
                        title = "یادداشت صوتی $dateStr",
                        filePath = file.absolutePath,
                        durationMs = finalDuration,
                        createdAt = now
                    )
                )
            }
            Toast.makeText(context, "یادداشت صوتی با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "مدت زمان ضبط بسیار کوتاه بود یا صدایی دریافت نشد", Toast.LENGTH_SHORT).show()
        }
    }

    fun playMemo(memo: VoiceMemoEntity) {
        try {
            try {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.stop()
                }
                mediaPlayer?.reset()
                mediaPlayer?.release()
            } catch (_: Exception) {}
            mediaPlayer = null

            if (currentPlayingId == memo.id) {
                currentPlayingId = null
                return
            }

            val file = File(memo.filePath)
            if (!file.exists() || file.length() == 0L) {
                Toast.makeText(context, "فایل صوتی در حافظه دستگاه یافت نشد", Toast.LENGTH_SHORT).show()
                currentPlayingId = null
                return
            }

            val player = MediaPlayer().apply {
                setDataSource(memo.filePath)
                prepare()
                start()
                setOnCompletionListener {
                    currentPlayingId = null
                }
            }
            mediaPlayer = player
            currentPlayingId = memo.id
        } catch (e: Exception) {
            e.printStackTrace()
            currentPlayingId = null
            Toast.makeText(context, "خطا در پخش فایل صوتی: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaRecorder?.stop()
            } catch (_: Exception) {}
            try {
                mediaRecorder?.release()
                mediaPlayer?.release()
            } catch (_: Exception) {}
        }
    }

    // Pulse animation for recording indicator
    val infiniteTransition = rememberInfiniteTransition(label = "rec_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

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
                        text = stringResource(R.string.voice_memos_title),
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Permission check banner
            if (!hasAudioPermission) {
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = stringResource(R.string.permission_needed_audio),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StudioButton(
                        text = stringResource(R.string.grant_permission),
                        onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Central Recording Controls Card
            StudioCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        val mins = recordingDurationSeconds / 60
                        val secs = recordingDurationSeconds % 60
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        StudioButton(
                            text = stringResource(R.string.stop_recording),
                            onClick = { stopRecording() },
                            icon = Icons.Default.Stop,
                            isPrimary = false
                        )
                    } else {
                        IconButton(
                            onClick = {
                                if (hasAudioPermission) {
                                    startRecording()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("record_audio_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = stringResource(R.string.record_audio),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.record_audio),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recorded Memos List
            if (memos.isEmpty()) {
                StudioEmptyState(
                    icon = Icons.Default.MicNone,
                    title = stringResource(R.string.no_voice_memos),
                    description = "ملودی‌ها و ایده‌های صوتی خود را ضبط کنید تا هرگز فراموش نشوند",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(memos, key = { it.id }) { memo ->
                        val isPlaying = currentPlayingId == memo.id
                        StudioCard(
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "voice_memo_${memo.id}"
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    IconButton(
                                        onClick = { playMemo(memo) },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) stringResource(R.string.cd_pause) else stringResource(R.string.cd_play),
                                            tint = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = memo.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        val mins = (memo.durationMs / 1000) / 60
                                        val secs = (memo.durationMs / 1000) % 60
                                        Text(
                                            text = String.format(Locale.US, "%02d:%02d", mins, secs),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            memoToRename = memo
                                            renameText = memo.title
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Rename", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { memoToDelete = memo },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.cd_delete),
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (memoToRename != null) {
        AlertDialog(
            onDismissRequest = { memoToRename = null },
            title = { Text("تغییر نام یادداشت صوتی") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        memoToRename?.let {
                            scope.launch { repo.renameVoiceMemo(it.id, renameText) }
                        }
                        memoToRename = null
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { memoToRename = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete Dialog
    StudioConfirmDialog(
        show = memoToDelete != null,
        title = stringResource(R.string.delete),
        message = "آیا از حذف این یادداشت صوتی اطمینان دارید؟",
        confirmText = stringResource(R.string.delete),
        cancelText = stringResource(R.string.cancel),
        isDestructive = true,
        onConfirm = {
            memoToDelete?.let {
                scope.launch { repo.deleteVoiceMemo(it.id) }
            }
            memoToDelete = null
        },
        onDismiss = { memoToDelete = null }
    )
}
