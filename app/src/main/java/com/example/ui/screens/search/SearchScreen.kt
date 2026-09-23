package com.example.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.LyricStudioApp
import com.example.R
import com.example.data.local.entity.IdeaEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.PunchlineEntity
import com.example.data.local.entity.WordBankEntity
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioEmptyState
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProject: (Long) -> Unit
) {
    val app = LyricStudioApp.instance
    val projectRepo = app.projectRepository
    val ideaRepo = app.ideaRepository
    val punchlineRepo = app.punchlineRepository
    val wordRepo = app.wordBankRepository

    var query by remember { mutableStateOf("") }
    var matchingProjects by remember { mutableStateOf<List<ProjectEntity>>(emptyList()) }
    var matchingIdeas by remember { mutableStateOf<List<IdeaEntity>>(emptyList()) }
    var matchingPunchlines by remember { mutableStateOf<List<PunchlineEntity>>(emptyList()) }
    var matchingWords by remember { mutableStateOf<List<WordBankEntity>>(emptyList()) }

    LaunchedEffect(query) {
        if (query.isBlank()) {
            matchingProjects = emptyList()
            matchingIdeas = emptyList()
            matchingPunchlines = emptyList()
            matchingWords = emptyList()
        } else {
            val allProjects = projectRepo.activeProjects.first()
            matchingProjects = allProjects.filter {
                it.title.contains(query, ignoreCase = true) || it.notes.contains(query, ignoreCase = true)
            }
            val allIdeas = ideaRepo.allIdeas.first()
            matchingIdeas = allIdeas.filter {
                it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true)
            }
            val allPunchlines = punchlineRepo.allPunchlines.first()
            matchingPunchlines = allPunchlines.filter {
                it.text.contains(query, ignoreCase = true)
            }
            val allWords = wordRepo.allWords.first()
            matchingWords = allWords.filter {
                it.word.contains(query, ignoreCase = true) || it.meaning.contains(query, ignoreCase = true)
            }
        }
    }

    val totalMatches = matchingProjects.size + matchingIdeas.size + matchingPunchlines.size + matchingWords.size

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp)
                            .testTag("global_search_input")
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (query.isBlank()) {
            StudioEmptyState(
                icon = Icons.Default.Search,
                title = "جستجو در تمام استودیو",
                description = "عنوان ترانه‌ها، متن‌ها، ایده‌ها، پانچ‌لاین‌ها و واژگان را یکجا جستجو کنید",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else if (totalMatches == 0) {
            StudioEmptyState(
                icon = Icons.Default.SearchOff,
                title = "نتیجه‌ای یافت نشد",
                description = "کلمه دیگری را برای جستجو وارد کنید",
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Projects section
                if (matchingProjects.isNotEmpty()) {
                    item {
                        Text(
                            text = "پروژه‌ها (${matchingProjects.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(matchingProjects) { p ->
                        StudioCard(
                            onClick = { onNavigateToProject(p.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = p.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(text = p.genre, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Ideas section
                if (matchingIdeas.isNotEmpty()) {
                    item {
                        Text(
                            text = "ایده‌ها (${matchingIdeas.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(matchingIdeas) { id ->
                        StudioCard(modifier = Modifier.fillMaxWidth()) {
                            Text(text = id.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            if (id.content.isNotBlank()) {
                                Text(text = id.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Punchlines section
                if (matchingPunchlines.isNotEmpty()) {
                    item {
                        Text(
                            text = "پانچ‌لاین‌ها (${matchingPunchlines.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(matchingPunchlines) { pl ->
                        StudioCard(modifier = Modifier.fillMaxWidth()) {
                            Text(text = pl.text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Words section
                if (matchingWords.isNotEmpty()) {
                    item {
                        Text(
                            text = "بانک واژگان (${matchingWords.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(matchingWords) { w ->
                        StudioCard(modifier = Modifier.fillMaxWidth()) {
                            Text(text = w.word, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            if (w.meaning.isNotBlank()) {
                                Text(text = w.meaning, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
