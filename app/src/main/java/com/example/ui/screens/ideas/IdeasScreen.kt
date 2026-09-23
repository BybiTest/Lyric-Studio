package com.example.ui.screens.ideas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.local.entity.IdeaEntity
import com.example.domain.model.IdeaCategory
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioConfirmDialog
import com.example.ui.components.StudioEmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeasScreen(
    onNavigateToProject: (Long) -> Unit,
    viewModel: IdeasViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var ideaToDelete by remember { mutableStateOf<IdeaEntity?>(null) }

    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(IdeaCategory.SongIdea) }

    val categories = listOf("All" to stringResource(R.string.filter_all)) +
            IdeaCategory.entries.map { it.name to it.faLabel }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.ideas_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavoritesFilter() }) {
                        Icon(
                            imageVector = if (state.onlyFavorites) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = stringResource(R.string.cd_favorite),
                            tint = if (state.onlyFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_idea_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_idea))
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
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("جستجو در ایده‌ها و خطوط...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { (key, label) ->
                    FilterChip(
                        selected = state.selectedCategory == key,
                        onClick = { viewModel.onCategorySelect(key) },
                        label = { Text(label) }
                    )
                }
            }

            // Items
            if (state.filteredIdeas.isEmpty() && !state.isLoading) {
                StudioEmptyState(
                    icon = Icons.Default.Lightbulb,
                    title = stringResource(R.string.no_ideas_title),
                    description = stringResource(R.string.no_ideas_desc),
                    actionText = stringResource(R.string.add_idea),
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.filteredIdeas, key = { it.id }) { idea ->
                        IdeaItemCard(
                            idea = idea,
                            onToggleFav = { viewModel.toggleFavorite(idea) },
                            onConvertToProject = {
                                viewModel.convertIdeaToProject(idea, onNavigateToProject)
                            },
                            onDelete = { ideaToDelete = idea }
                        )
                    }
                }
            }
        }
    }

    // Add Idea Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.add_idea),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text(stringResource(R.string.idea_title_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "دسته‌بندی ایده", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(IdeaCategory.entries) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.faLabel) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        label = { Text(stringResource(R.string.idea_content_hint)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addIdea(newTitle, newContent, selectedCategory.name)
                        showAddDialog = false
                        newTitle = ""
                        newContent = ""
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete Idea Dialog
    StudioConfirmDialog(
        show = ideaToDelete != null,
        title = stringResource(R.string.delete),
        message = "آیا از حذف این ایده مطمئن هستید؟",
        confirmText = stringResource(R.string.delete),
        cancelText = stringResource(R.string.cancel),
        isDestructive = true,
        onConfirm = {
            ideaToDelete?.let { viewModel.deleteIdea(it.id) }
            ideaToDelete = null
        },
        onDismiss = { ideaToDelete = null }
    )
}

@Composable
private fun IdeaItemCard(
    idea: IdeaEntity,
    onToggleFav: () -> Unit,
    onConvertToProject: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    StudioCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "idea_card_${idea.id}"
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
                    text = IdeaCategory.fromString(idea.category).faLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFav, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (idea.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = stringResource(R.string.cd_favorite),
                        tint = if (idea.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.convert_to_project)) },
                            onClick = {
                                showMenu = false
                                onConvertToProject()
                            },
                            leadingIcon = { Icon(Icons.Default.DriveFileMove, contentDescription = null) }
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = idea.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (idea.content.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = idea.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
