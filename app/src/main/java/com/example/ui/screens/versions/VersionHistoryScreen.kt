package com.example.ui.screens.versions

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
import com.example.data.local.entity.SectionEntity
import com.example.data.local.entity.VersionEntity
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioConfirmDialog
import com.example.ui.components.StudioEmptyState
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionHistoryScreen(
    projectId: Long,
    onNavigateBack: () -> Unit
) {
    val app = LyricStudioApp.instance
    val versionRepo = app.versionRepository
    val sectionRepo = app.sectionRepository
    val scope = rememberCoroutineScope()

    val versions by versionRepo.getVersionsForProject(projectId).collectAsState(initial = emptyList())
    var versionToRestore by remember { mutableStateOf<VersionEntity?>(null) }
    var versionToDelete by remember { mutableStateOf<VersionEntity?>(null) }

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
                        text = stringResource(R.string.version_history_title),
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
        if (versions.isEmpty()) {
            StudioEmptyState(
                icon = Icons.Default.History,
                title = stringResource(R.string.no_versions),
                description = "در حین سرودن ترانه، می‌توانید از نسخه‌ها اسنپ‌شات بگیرید تا در صورت نیاز به عقب برگردید",
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
                items(versions, key = { it.id }) { version ->
                    StudioCard(
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "version_card_${version.id}"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = version.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(Date(version.createdAt))
                                Text(
                                    text = "$dateStr • ${version.wordCount} کلمه • ${version.lineCount} خط",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { versionToRestore = version }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = stringResource(R.string.restore_version),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { versionToDelete = version }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.cd_delete),
                                        tint = MaterialTheme.colorScheme.error
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
        show = versionToRestore != null,
        title = stringResource(R.string.restore_version),
        message = "آیا می‌خواهید متن ترانه را به این نسخه بازگردانید؟",
        confirmText = stringResource(R.string.confirm),
        cancelText = stringResource(R.string.cancel),
        onConfirm = {
            versionToRestore?.let { v ->
                scope.launch {
                    try {
                        val arr = JSONArray(v.fullSnapshotJson)
                        val sections = mutableListOf<SectionEntity>()
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            sections.add(
                                SectionEntity(
                                    projectId = projectId,
                                    type = obj.getString("type"),
                                    customTitle = obj.optString("customTitle", ""),
                                    content = obj.optString("content", ""),
                                    orderIndex = obj.optInt("orderIndex", i)
                                )
                            )
                        }
                        sectionRepo.deleteSectionsForProject(projectId)
                        for (s in sections) {
                            sectionRepo.insertSection(s)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            versionToRestore = null
        },
        onDismiss = { versionToRestore = null }
    )

    // Confirm Delete
    StudioConfirmDialog(
        show = versionToDelete != null,
        title = stringResource(R.string.delete),
        message = "آیا از حذف این نسخه اطمینان دارید؟",
        confirmText = stringResource(R.string.delete),
        cancelText = stringResource(R.string.cancel),
        isDestructive = true,
        onConfirm = {
            versionToDelete?.let {
                scope.launch { versionRepo.deleteVersion(it.id) }
            }
            versionToDelete = null
        },
        onDismiss = { versionToDelete = null }
    )
}
