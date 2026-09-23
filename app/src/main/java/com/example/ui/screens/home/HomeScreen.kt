package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.local.entity.ProjectEntity
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProject: (Long) -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToQuickWriting: () -> Unit,
    onNavigateToIdeas: () -> Unit,
    onNavigateToVoice: () -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var newProjectTitle by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("Rap") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = state.greetingRes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(id = R.string.cd_search),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Last Project Banner
            item {
                if (state.latestProject != null) {
                    val proj = state.latestProject!!
                    StudioCard(
                        testTag = "last_project_card",
                        onClick = { onNavigateToProject(proj.id) },
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.home_last_project),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = proj.genre,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = proj.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val dateFormatted = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(proj.lastEditedAt))
                        Text(
                            text = "${stringResource(R.string.sort_date_desc)}: $dateFormatted",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        StudioButton(
                            text = stringResource(id = R.string.home_continue_writing),
                            onClick = { onNavigateToProject(proj.id) },
                            icon = Icons.Default.Edit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // Call to create first project
                    StudioCard(
                        testTag = "first_project_card",
                        onClick = { showNewProjectDialog = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.home_new_project),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.no_projects_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 2. Quick Actions
            item {
                Text(
                    text = stringResource(id = R.string.home_quick_actions),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionItem(
                        icon = Icons.Default.Add,
                        label = stringResource(id = R.string.home_new_project),
                        onClick = { showNewProjectDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionItem(
                        icon = Icons.Default.FlashOn,
                        label = stringResource(id = R.string.home_quick_writing),
                        onClick = onNavigateToQuickWriting,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionItem(
                        icon = Icons.Default.Lightbulb,
                        label = stringResource(id = R.string.home_new_idea),
                        onClick = onNavigateToIdeas,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionItem(
                        icon = Icons.Default.Mic,
                        label = stringResource(id = R.string.home_new_voice),
                        onClick = onNavigateToVoice,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Studio Stats Row
            item {
                Text(
                    text = stringResource(id = R.string.home_stats),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(
                        value = "${state.totalProjects}",
                        label = stringResource(id = R.string.home_total_projects),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        value = "${state.totalWords}",
                        label = stringResource(id = R.string.home_total_words),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        value = "${state.currentStreak} ${stringResource(id = R.string.days)}",
                        label = stringResource(id = R.string.home_current_streak),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Recent Projects
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.home_recent_projects),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToProjects) {
                        Text(
                            text = stringResource(id = R.string.filter_all),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            if (state.recentProjects.isEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.no_projects_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(state.recentProjects) { project ->
                    RecentProjectRow(
                        project = project,
                        onClick = { onNavigateToProject(project.id) }
                    )
                }
            }
        }
    }

    if (showNewProjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.home_new_project),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newProjectTitle,
                        onValueChange = { newProjectTitle = it },
                        label = { Text(stringResource(id = R.string.project_title_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val genres = listOf("Rap", "Hip-Hop", "Pop", "R&B", "Rock", "Poetry")
                    Text(
                        text = stringResource(id = R.string.genre_hint),
                        style = MaterialTheme.typography.labelMedium
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(genres) { g ->
                            FilterChip(
                                selected = selectedGenre == g,
                                onClick = { selectedGenre = g },
                                label = { Text(g) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createNewProject(newProjectTitle, selectedGenre) { id ->
                            showNewProjectDialog = false
                            newProjectTitle = ""
                            onNavigateToProject(id)
                        }
                    }
                ) {
                    Text(stringResource(id = R.string.create_project))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    StudioCard(
        onClick = onClick,
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecentProjectRow(
    project: ProjectEntity,
    onClick: () -> Unit
) {
    StudioCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val date = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(project.lastEditedAt))
                Text(
                    text = "${project.genre} • $date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
