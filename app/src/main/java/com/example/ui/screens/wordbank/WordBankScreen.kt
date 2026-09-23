package com.example.ui.screens.wordbank

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.LyricStudioApp
import com.example.R
import com.example.data.local.entity.WordBankEntity
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioConfirmDialog
import com.example.ui.components.StudioEmptyState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordBankScreen(
    onNavigateBack: () -> Unit
) {
    val app = LyricStudioApp.instance
    val repo = app.wordBankRepository
    val scope = rememberCoroutineScope()

    val allWords by repo.allWords.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var onlyFavorites by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var wordToDelete by remember { mutableStateOf<WordBankEntity?>(null) }
    var newWord by remember { mutableStateOf("") }
    var newMeaning by remember { mutableStateOf("") }

    val filtered = remember(allWords, searchQuery, onlyFavorites) {
        allWords.filter { item ->
            val matchesQuery = searchQuery.isBlank() ||
                    item.word.contains(searchQuery, ignoreCase = true) ||
                    item.meaning.contains(searchQuery, ignoreCase = true)
            val matchesFav = !onlyFavorites || item.isFavorite
            matchesQuery && matchesFav
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
                        text = stringResource(R.string.word_bank_title),
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
                modifier = Modifier.testTag("add_word_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_word))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجو در واژگان و اصطلاحات...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            if (filtered.isEmpty()) {
                StudioEmptyState(
                    icon = Icons.Default.MenuBook,
                    title = stringResource(R.string.no_words_title),
                    description = stringResource(R.string.no_words_desc),
                    actionText = stringResource(R.string.add_word),
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { wordItem ->
                        StudioCard(
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "word_card_${wordItem.id}"
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = wordItem.word,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                repo.updateWord(wordItem.copy(isFavorite = !wordItem.isFavorite))
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (wordItem.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = stringResource(R.string.cd_favorite),
                                            tint = if (wordItem.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { wordToDelete = wordItem },
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

                            if (wordItem.meaning.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = wordItem.meaning,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Word Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.add_word),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newWord,
                        onValueChange = { newWord = it },
                        label = { Text(stringResource(R.string.word_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newMeaning,
                        onValueChange = { newMeaning = it },
                        label = { Text(stringResource(R.string.meaning_hint)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 80.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newWord.isNotBlank()) {
                            scope.launch {
                                repo.insertWord(
                                    WordBankEntity(
                                        word = newWord.trim(),
                                        meaning = newMeaning.trim()
                                    )
                                )
                            }
                            showAddDialog = false
                            newWord = ""
                            newMeaning = ""
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

    // Delete Word Dialog
    StudioConfirmDialog(
        show = wordToDelete != null,
        title = stringResource(R.string.delete),
        message = "آیا از حذف این واژه اطمینان دارید؟",
        confirmText = stringResource(R.string.delete),
        cancelText = stringResource(R.string.cancel),
        isDestructive = true,
        onConfirm = {
            wordToDelete?.let {
                scope.launch { repo.deleteWord(it.id) }
            }
            wordToDelete = null
        },
        onDismiss = { wordToDelete = null }
    )
}
