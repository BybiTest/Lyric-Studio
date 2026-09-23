package com.example.ui.screens.editor

import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.SectionEntity
import com.example.domain.model.SectionType
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    projectId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToMetronome: () -> Unit,
    onNavigateToRhymes: () -> Unit,
    onNavigateToVersions: (Long) -> Unit,
    viewModel: EditorViewModel = remember(projectId) { EditorViewModel(projectId) }
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showAddSectionDialog by remember { mutableStateOf(false) }
    var showProjectSettingsDialog by remember { mutableStateOf(false) }
    var sectionToDelete by remember { mutableStateOf<SectionEntity?>(null) }
    var showSnapshotDialog by remember { mutableStateOf(false) }
    var snapshotLabel by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Section Type selection for add dialog
    var selectedSectionType by remember { mutableStateOf(SectionType.Verse) }
    var customSectionTitle by remember { mutableStateOf("") }

    // Editable project info state
    var editTitle by remember { mutableStateOf("") }
    var editBpm by remember { mutableStateOf("") }
    var editKey by remember { mutableStateOf("") }
    var editStatus by remember { mutableStateOf("") }

    LaunchedEffect(state.project) {
        state.project?.let {
            editTitle = it.title
            editBpm = it.bpm?.toString() ?: ""
            editKey = it.musicalKey
            editStatus = it.status
        }
    }

    Scaffold(
        topBar = {
            if (!state.isFocusMode) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("editor_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back)
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = state.project?.title ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Live stats
                                Text(
                                    text = stringResource(
                                        R.string.editor_word_count_format,
                                        state.totalWords,
                                        state.totalLines
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Autosave indicator (§11)
                                if (state.isSaving) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(10.dp),
                                            strokeWidth = 1.5.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.editor_saving),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else if (state.lastSavedTimestamp > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = stringResource(R.string.editor_auto_saved),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.toggleFocusMode() },
                            modifier = Modifier.testTag("focus_mode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = stringResource(R.string.cd_focus_mode)
                            )
                        }
                        IconButton(onClick = onNavigateToRhymes) {
                            Icon(Icons.Default.MenuBook, contentDescription = stringResource(R.string.tool_rhyme))
                        }
                        IconButton(onClick = onNavigateToMetronome) {
                            Icon(Icons.Default.Timer, contentDescription = stringResource(R.string.tool_metronome))
                        }
                        IconButton(onClick = { showProjectSettingsDialog = true }) {
                            Icon(Icons.Default.Tune, contentDescription = stringResource(R.string.settings_title))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        floatingActionButton = {
            if (state.isFocusMode) {
                // Focus Mode Floating Exit Button (§12)
                SmallFloatingActionButton(
                    onClick = { viewModel.toggleFocusMode() },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("exit_focus_mode_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = stringResource(R.string.cd_exit_focus)
                    )
                }
            } else {
                FloatingActionButton(
                    onClick = { showAddSectionDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_section_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.editor_add_section)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (state.isFocusMode) 24.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                // In Focus Mode, show clean header stats
                if (state.isFocusMode) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.project?.title ?: "",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(
                                    R.string.editor_word_count_format,
                                    state.totalWords,
                                    state.totalLines
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Sections
                items(state.sections, key = { it.id }) { section ->
                    SectionItemCard(
                        section = section,
                        fontSize = state.fontSize,
                        lineHeight = state.lineHeight,
                        letterSpacing = state.letterSpacing,
                        isFocusMode = state.isFocusMode,
                        onContentChange = { newContent ->
                            viewModel.updateSectionContent(section.id, newContent)
                        },
                        onMoveUp = { viewModel.moveSectionUp(section.id) },
                        onMoveDown = { viewModel.moveSectionDown(section.id) },
                        onDelete = { sectionToDelete = section }
                    )
                }

                // Add Section Button row at bottom
                if (!state.isFocusMode) {
                    item {
                        StudioButton(
                            text = stringResource(R.string.editor_add_section),
                            onClick = { showAddSectionDialog = true },
                            icon = Icons.Default.Add,
                            isPrimary = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Extra Action Shortcuts
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StudioButton(
                                text = stringResource(R.string.editor_versions),
                                onClick = { onNavigateToVersions(projectId) },
                                icon = Icons.Default.History,
                                isPrimary = false,
                                modifier = Modifier.weight(1f)
                            )
                            StudioButton(
                                text = stringResource(R.string.editor_export),
                                onClick = {
                                    val fullLyrics = viewModel.getFullLyricsText()
                                    val projectTitle = state.project?.title ?: "ترانه"
                                    if (fullLyrics.isBlank()) {
                                        Toast.makeText(context, "متنی برای خروجی گرفتن وجود ندارد", Toast.LENGTH_SHORT).show()
                                    } else {
                                        try {
                                            val safeFileName = projectTitle
                                                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                                                .trim()
                                                .take(40)
                                                .ifBlank { "lyrics" } + ".txt"

                                            val exportFile = File(context.cacheDir, safeFileName)
                                            exportFile.writeText(fullLyrics)

                                            val authority = "${context.packageName}.fileprovider"
                                            val contentUri = FileProvider.getUriForFile(context, authority, exportFile)

                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, projectTitle)
                                                putExtra(Intent.EXTRA_TEXT, fullLyrics)
                                                putExtra(Intent.EXTRA_STREAM, contentUri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            val chooser = Intent.createChooser(shareIntent, "خروجی و اشتراک‌گذاری ترانه").apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(chooser)
                                        } catch (e: Exception) {
                                            try {
                                                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_SUBJECT, projectTitle)
                                                    putExtra(Intent.EXTRA_TEXT, fullLyrics)
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                val chooser = Intent.createChooser(fallbackIntent, "اشتراک‌گذاری ترانه").apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                context.startActivity(chooser)
                                            } catch (ex: Exception) {
                                                Toast.makeText(context, "خطا در خروجی گرفتن: ${ex.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                icon = Icons.Default.Share,
                                isPrimary = false,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Section Dialog
    if (showAddSectionDialog) {
        val sectionTypes = SectionType.entries
        AlertDialog(
            onDismissRequest = { showAddSectionDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.editor_add_section),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "نوع بخش (Section Type)",
                        style = MaterialTheme.typography.labelMedium
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(sectionTypes) { type ->
                            FilterChip(
                                selected = selectedSectionType == type,
                                onClick = { selectedSectionType = type },
                                label = { Text(type.faName) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = customSectionTitle,
                        onValueChange = { customSectionTitle = it },
                        label = { Text(stringResource(R.string.editor_section_name)) },
                        placeholder = { Text(selectedSectionType.faName) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = customSectionTitle.ifBlank { selectedSectionType.faName }
                        viewModel.addSection(selectedSectionType.name, title)
                        showAddSectionDialog = false
                        customSectionTitle = ""
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSectionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Project Info & Musical Settings Dialog
    if (showProjectSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showProjectSettingsDialog = false },
            title = {
                Text(
                    text = "مشخصات ترانه و تنظیمات موسیقی",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text(stringResource(R.string.project_title_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editBpm,
                            onValueChange = { editBpm = it },
                            label = { Text(stringResource(R.string.editor_bpm)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editKey,
                            onValueChange = { editKey = it },
                            label = { Text(stringResource(R.string.editor_key)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val statuses = listOf("Writing", "Editing", "Ready", "Completed", "Archived")
                    Text(text = "وضعیت ترانه", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(statuses) { st ->
                            FilterChip(
                                selected = editStatus == st,
                                onClick = { editStatus = st },
                                label = { Text(st) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bpmInt = editBpm.toIntOrNull()
                        viewModel.updateProjectDetails(editTitle, bpmInt, editKey, editStatus)
                        showProjectSettingsDialog = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showProjectSettingsDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Confirm Delete Section
    StudioConfirmDialog(
        show = sectionToDelete != null,
        title = stringResource(R.string.delete),
        message = "آیا از حذف این بخش اطمینان دارید؟",
        confirmText = stringResource(R.string.delete),
        cancelText = stringResource(R.string.cancel),
        isDestructive = true,
        onConfirm = {
            sectionToDelete?.let { viewModel.deleteSection(it.id) }
            sectionToDelete = null
        },
        onDismiss = { sectionToDelete = null }
    )
}

@Composable
private fun SectionItemCard(
    section: SectionEntity,
    fontSize: Float,
    lineHeight: Float,
    letterSpacing: Float,
    isFocusMode: Boolean,
    onContentChange: (String) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    val sectionWords = remember(section.content) {
        val t = section.content.trim()
        if (t.isEmpty()) 0 else t.split("\\s+".toRegex()).size
    }
    val sectionLines = remember(section.content) {
        val t = section.content.trim()
        if (t.isEmpty()) 0 else t.lines().size
    }

    var showMenu by remember { mutableStateOf(false) }

    StudioCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = if (isFocusMode) 8.dp else 16.dp,
        backgroundColor = if (isFocusMode) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
        testTag = "section_card_${section.id}"
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (section.customTitle.isNotBlank()) section.customTitle else section.type,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = "$sectionWords کلمه • $sectionLines خط",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isFocusMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMoveUp,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onMoveDown,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(16.dp))
                    }
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large Multiline TextField with unlimited writing capacity (§10)
        OutlinedTextField(
            value = section.content,
            onValueChange = onContentChange,
            placeholder = {
                Text(
                    text = "شروع به نوشتن ابیات، کلمات و ترانه کنید...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
            },
            textStyle = TextStyle(
                fontSize = fontSize.sp,
                lineHeight = (fontSize * lineHeight).sp,
                letterSpacing = letterSpacing.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 120.dp)
                .testTag("section_editor_input_${section.id}"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}
