package com.example.ui.screens.punchlines

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.LyricStudioApp
import com.example.R
import com.example.data.local.entity.PunchlineEntity
import com.example.domain.model.PunchlineType
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioConfirmDialog
import com.example.ui.components.StudioEmptyState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PunchlinesScreen(
    onNavigateBack: () -> Unit
) {
    val app = LyricStudioApp.instance
    val repo = app.punchlineRepository
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val allPunchlines by repo.allPunchlines.collectAsState(initial = emptyList())
    var selectedType by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var onlyFavorites by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var punchlineToDelete by remember { mutableStateOf<PunchlineEntity?>(null) }
    var newText by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf(PunchlineType.Punchline) }

    val filtered = remember(allPunchlines, selectedType, searchQuery, onlyFavorites) {
        allPunchlines.filter { item ->
            val matchesType = selectedType == "All" || item.type.equals(selectedType, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() || item.text.contains(searchQuery, ignoreCase = true)
            val matchesFav = !onlyFavorites || item.isFavorite
            matchesType && matchesQuery && matchesFav
        }
    }

    val typeFilters = listOf("All" to stringResource(R.string.filter_all)) +
            PunchlineType.entries.map { it.name to it.faLabel }

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
                        text = stringResource(R.string.punchlines_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { onlyFavorites = !onlyFavorites }) {
                        Icon(
                            imageVector = if (onlyFavorites) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = stringResource(R.string.cd_favorite),
                            tint = if (onlyFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                modifier = Modifier.testTag("add_punchline_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_punchline))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجو در پانچ‌لاین‌ها و بیت‌های طلایی...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Type filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(typeFilters) { (key, label) ->
                    FilterChip(
                        selected = selectedType == key,
                        onClick = { selectedType = key },
                        label = { Text(label) }
                    )
                }
            }

            if (filtered.isEmpty()) {
                StudioEmptyState(
                    icon = Icons.Default.Bolt,
                    title = stringResource(R.string.no_punchlines_title),
                    description = stringResource(R.string.no_punchlines_desc),
                    actionText = stringResource(R.string.add_punchline),
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { punchline ->
                        StudioCard(
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "punchline_card_${punchline.id}"
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
                                        text = PunchlineType.fromString(punchline.type).faLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // 1-Tap Copy
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(punchline.text))
                                            Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = stringResource(R.string.copy_to_clipboard),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    // Favorite toggle
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                repo.updatePunchline(punchline.copy(isFavorite = !punchline.isFavorite))
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (punchline.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = stringResource(R.string.cd_favorite),
                                            tint = if (punchline.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    // Delete
                                    IconButton(
                                        onClick = { punchlineToDelete = punchline },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.cd_delete),
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = punchline.text,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Punchline Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.add_punchline),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "نوع پانچ‌لاین", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(PunchlineType.entries) { type ->
                            FilterChip(
                                selected = newType == type,
                                onClick = { newType = type },
                                label = { Text(type.faLabel) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = newText,
                        onValueChange = { newText = it },
                        label = { Text(stringResource(R.string.punchline_hint)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newText.isNotBlank()) {
                            scope.launch {
                                repo.insertPunchline(
                                    PunchlineEntity(
                                        text = newText.trim(),
                                        type = newType.name
                                    )
                                )
                            }
                            showAddDialog = false
                            newText = ""
                        }
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

    // Delete Confirmation
    StudioConfirmDialog(
        show = punchlineToDelete != null,
        title = stringResource(R.string.delete),
        message = "آیا از حذف این پانچ‌لاین اطمینان دارید؟",
        confirmText = stringResource(R.string.delete),
        cancelText = stringResource(R.string.cancel),
        isDestructive = true,
        onConfirm = {
            punchlineToDelete?.let {
                scope.launch { repo.deletePunchline(it.id) }
            }
            punchlineToDelete = null
        },
        onDismiss = { punchlineToDelete = null }
    )
}
