package com.example.ui.screens.quick

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.LyricStudioApp
import com.example.R
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.SectionEntity
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioConfirmDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickWritingScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProject: (Long) -> Unit
) {
    val app = LyricStudioApp.instance
    val quickRepo = app.quickNoteRepository
    val projectRepo = app.projectRepository
    val sectionRepo = app.sectionRepository
    val streakRepo = app.streakRepository
    val scope = rememberCoroutineScope()

    val quickNote by quickRepo.quickNote.collectAsState(initial = null)
    var content by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }
    var showConvertToProjectDialog by remember { mutableStateOf(false) }
    var newProjectTitle by remember { mutableStateOf("") }

    LaunchedEffect(quickNote) {
        quickNote?.let {
            if (content.isEmpty() && it.content.isNotEmpty()) {
                content = it.content
            }
        }
    }

    val wordCount = remember(content) {
        val t = content.trim()
        if (t.isEmpty()) 0 else t.split("\\s+".toRegex()).size
    }
    val lineCount = remember(content) {
        val t = content.trim()
        if (t.isEmpty()) 0 else t.lines().size
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
                    Column {
                        Text(
                            text = stringResource(R.string.quick_writing_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$wordCount ${stringResource(R.string.words)} • $lineCount ${stringResource(R.string.lines)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (content.isNotBlank()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = stringResource(R.string.clear))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StudioButton(
                        text = stringResource(R.string.convert_to_project),
                        onClick = {
                            if (content.isNotBlank()) {
                                showConvertToProjectDialog = true
                            }
                        },
                        icon = Icons.Default.DriveFileMove,
                        enabled = content.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
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
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    scope.launch {
                        quickRepo.saveNote(it)
                        streakRepo.recordWritingActivity(1)
                    }
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.quick_writing_hint),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                },
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    lineHeight = 26.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("quick_writing_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }

    // Convert Dialog
    if (showConvertToProjectDialog) {
        AlertDialog(
            onDismissRequest = { showConvertToProjectDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.convert_to_project),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "عنوان مناسبی برای پروژه جدید انتخاب کنید:")
                    OutlinedTextField(
                        value = newProjectTitle,
                        onValueChange = { newProjectTitle = it },
                        label = { Text(stringResource(R.string.project_title_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val title = newProjectTitle.ifBlank { "ترانه نوشتن سریع" }
                            val projectId = projectRepo.createProject(
                                ProjectEntity(title = title)
                            )
                            sectionRepo.insertSection(
                                SectionEntity(
                                    projectId = projectId,
                                    type = "Verse",
                                    customTitle = "بند آغازین",
                                    content = content,
                                    orderIndex = 0
                                )
                            )
                            // Clear quick pad after conversion
                            quickRepo.saveNote("")
                            content = ""
                            showConvertToProjectDialog = false
                            onNavigateToProject(projectId)
                        }
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConvertToProjectDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Clear Confirmation
    StudioConfirmDialog(
        show = showClearDialog,
        title = stringResource(R.string.clear),
        message = "آیا از پاک کردن متن نوشتن سریع اطمینان دارید؟",
        confirmText = stringResource(R.string.delete),
        cancelText = stringResource(R.string.cancel),
        isDestructive = true,
        onConfirm = {
            scope.launch {
                quickRepo.saveNote("")
                content = ""
            }
            showClearDialog = false
        },
        onDismiss = { showClearDialog = false }
    )
}
