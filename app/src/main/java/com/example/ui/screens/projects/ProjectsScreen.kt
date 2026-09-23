package com.example.ui.screens.projects

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.local.entity.ProjectEntity
import com.example.domain.model.ProjectStatus
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioConfirmDialog
import com.example.ui.components.StudioEmptyState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onNavigateToProject: (Long) -> Unit,
    viewModel: ProjectsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Dialog inputs
    var newTitle by remember { mutableStateOf("") }
    var newGenre by remember { mutableStateOf("Rap") }
    var newBpm by remember { mutableStateOf("") }
    var newKey by remember { mutableStateOf("") }

    val statusFilters = listOf(
        "All" to stringResource(R.string.filter_all),
        "Idea" to stringResource(R.string.status_idea),
        "Writing" to stringResource(R.string.status_writing),
        "Editing" to stringResource(R.string.status_editing),
        "Ready" to stringResource(R.string.status_ready),
        "Completed" to stringResource(R.string.status_completed),
        "Archived" to stringResource(R.string.status_archived)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.projects_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleGridView() },
                        modifier = Modifier.testTag("toggle_grid_button")
                    ) {
                        Icon(
                            imageVector = if (state.isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = stringResource(R.string.cd_toggle_grid)
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sort_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = stringResource(R.string.cd_sort)
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_date_desc)) },
                                onClick = {
                                    viewModel.onSortOrderChanged(SortOrder.DateDesc)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_date_asc)) },
                                onClick = {
                                    viewModel.onSortOrderChanged(SortOrder.DateAsc)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_title)) },
                                onClick = {
                                    viewModel.onSortOrderChanged(SortOrder.Title)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_word_count)) },
                                onClick = {
                                    viewModel.onSortOrderChanged(SortOrder.WordCount)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewProjectDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_project_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_project))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("project_search_field")
            )

            // Status Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statusFilters) { (key, label) ->
                    FilterChip(
                        selected = state.selectedStatus == key,
                        onClick = { viewModel.onStatusSelected(key) },
                        label = { Text(label) },
                        modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                    )
                }
            }

            // Project Items
            if (state.filteredProjects.isEmpty() && !state.isLoading) {
                StudioEmptyState(
                    icon = Icons.Default.MusicNote,
                    title = stringResource(R.string.no_projects_title),
                    description = stringResource(R.string.no_projects_desc),
                    actionText = stringResource(R.string.create_project),
                    onActionClick = { showNewProjectDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                if (state.isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.filteredProjects, key = { it.project.id }) { item ->
                            ProjectCardGrid(
                                item = item,
                                onClick = { onNavigateToProject(item.project.id) },
                                onDuplicate = { viewModel.duplicateProject(item.project) },
                                onDelete = { projectToDelete = item.project }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.filteredProjects, key = { it.project.id }) { item ->
                            ProjectCardList(
                                item = item,
                                onClick = { onNavigateToProject(item.project.id) },
                                onDuplicate = { viewModel.duplicateProject(item.project) },
                                onDelete = { projectToDelete = item.project }
                            )
                        }
                    }
                }
            }
        }
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.create_project),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text(stringResource(R.string.project_title_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val genres = listOf("Rap", "Hip-Hop", "Pop", "R&B", "Rock", "Poetry", "Freestyle")
                    Text(
                        text = stringResource(R.string.genre_hint),
                        style = MaterialTheme.typography.labelSmall
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(genres) { g ->
                            FilterChip(
                                selected = newGenre == g,
                                onClick = { newGenre = g },
                                label = { Text(g) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newBpm,
                            onValueChange = { newBpm = it },
                            label = { Text(stringResource(R.string.bpm_hint)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newKey,
                            onValueChange = { newKey = it },
                            label = { Text(stringResource(R.string.key_hint)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bpmInt = newBpm.toIntOrNull()
                        viewModel.createProject(newTitle, newGenre, bpmInt, newKey) { id ->
                            showNewProjectDialog = false
                            newTitle = ""
                            newBpm = ""
                            newKey = ""
                            onNavigateToProject(id)
                        }
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Confirm Delete Dialog
    StudioConfirmDialog(
        show = projectToDelete != null,
        title = stringResource(R.string.delete),
        message = stringResource(R.string.confirm_delete_project),
        confirmText = stringResource(R.string.delete),
        cancelText = stringResource(R.string.cancel),
        isDestructive = true,
        onConfirm = {
            projectToDelete?.let { viewModel.moveToTrash(it.id) }
            projectToDelete = null
        },
        onDismiss = { projectToDelete = null }
    )
}

@Composable
private fun ProjectCardList(
    item: ProjectItemUi,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    StudioCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        testTag = "project_item_${item.project.id}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = item.project.genre,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = item.project.status,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (item.project.bpm != null) {
                        Text(
                            text = "${item.project.bpm} BPM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (item.project.musicalKey.isNotBlank()) {
                        Text(
                            text = item.project.musicalKey,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.project.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                val dateFormatted = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(item.project.lastEditedAt))
                Text(
                    text = "${item.wordCount} ${stringResource(R.string.words)} • ${item.lineCount} ${stringResource(R.string.lines)} • $dateFormatted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_menu))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.editor_continue_writing ?: R.string.home_continue_writing)) },
                        onClick = {
                            showMenu = false
                            onClick()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("کپی (Duplicate)") },
                        onClick = {
                            showMenu = false
                            onDuplicate()
                        },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
                    )
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

@Composable
private fun ProjectCardGrid(
    item: ProjectItemUi,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    StudioCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        testTag = "project_grid_item_${item.project.id}"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = item.project.genre,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("کپی") },
                        onClick = {
                            showMenu = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.project.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${item.wordCount} ${stringResource(R.string.words)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )

        val dateFormatted = SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(item.project.lastEditedAt))
        Text(
            text = dateFormatted,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
