package com.example.ui.screens.trash

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.LyricStudioApp
import com.example.R
import com.example.data.local.entity.ProjectEntity
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioConfirmDialog
import com.example.ui.components.StudioEmptyState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onNavigateBack: () -> Unit
) {
    val app = LyricStudioApp.instance
    val projectRepo = app.projectRepository
    val scope = rememberCoroutineScope()

    val trashedProjects by projectRepo.trashProjects.collectAsState(initial = emptyList())
    var projectToRestore by remember { mutableStateOf<ProjectEntity?>(null) }
    var projectToDeletePermanently by remember { mutableStateOf<ProjectEntity?>(null) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }

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
                        text = stringResource(R.string.trash_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (trashedProjects.isNotEmpty()) {
                        IconButton(onClick = { showEmptyTrashDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = stringResource(R.string.empty_trash),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (trashedProjects.isEmpty()) {
            StudioEmptyState(
                icon = Icons.Default.DeleteOutline,
                title = stringResource(R.string.empty_trash),
                description = "سطل زباله خالی است",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trashedProjects, key = { it.id }) { project ->
                    StudioCard(
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "trash_item_${project.id}"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(project.lastEditedAt))
                                Text(
                                    text = "${project.genre} • $dateStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { projectToRestore = project },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = stringResource(R.string.restore_project),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { projectToDeletePermanently = project },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = stringResource(R.string.delete_permanently),
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirm Restore
    StudioConfirmDialog(
        show = projectToRestore != null,
        title = stringResource(R.string.restore_project),
        message = "آیا می‌خواهید پروژه '${projectToRestore?.title}' بازیابی شود؟",
        confirmText = stringResource(R.string.restore_project),
        cancelText = stringResource(R.string.cancel),
        onConfirm = {
            projectToRestore?.let {
                scope.launch { projectRepo.restoreProject(it.id) }
            }
            projectToRestore = null
        },
        onDismiss = { projectToRestore = null }
    )

    // Confirm Permanent Delete
    StudioConfirmDialog(
        show = projectToDeletePermanently != null,
        title = stringResource(R.string.delete_permanently),
        message = "آیا از حذف دائمی این پروژه اطمینان دارید؟ این عمل غیرقابل بازگشت است.",
        confirmText = stringResource(R.string.delete),
        cancelText = stringResource(R.string.cancel),
        isDestructive = true,
        onConfirm = {
            projectToDeletePermanently?.let {
                scope.launch { projectRepo.deletePermanently(it.id) }
            }
            projectToDeletePermanently = null
        },
        onDismiss = { projectToDeletePermanently = null }
    )

    // Empty All Trash Dialog
    StudioConfirmDialog(
        show = showEmptyTrashDialog,
        title = stringResource(R.string.empty_trash),
        message = "آیا می‌خواهید تمام پروژه‌های سطل زباله به طور دائم حذف شوند؟",
        confirmText = stringResource(R.string.delete),
        cancelText = stringResource(R.string.cancel),
        isDestructive = true,
        onConfirm = {
            scope.launch {
                projectRepo.emptyTrash()
            }
            showEmptyTrashDialog = false
        },
        onDismiss = { showEmptyTrashDialog = false }
    )
}
