package com.example.ui.screens.templates

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
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.SectionEntity
import com.example.data.local.entity.TemplateEntity
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioCard
import kotlinx.coroutines.launch
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProject: (Long) -> Unit
) {
    val app = LyricStudioApp.instance
    val repo = app.templateRepository
    val projectRepo = app.projectRepository
    val sectionRepo = app.sectionRepository
    val scope = rememberCoroutineScope()

    val templates by repo.allTemplates.collectAsState(initial = emptyList())
    var showCreateCustomDialog by remember { mutableStateOf(false) }

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
                        text = stringResource(R.string.templates_title),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(templates, key = { it.id }) { template ->
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "template_card_${template.id}"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = template.genre,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    StudioButton(
                        text = stringResource(R.string.apply_template),
                        onClick = {
                            scope.launch {
                                val projectId = projectRepo.createProject(
                                    ProjectEntity(
                                        title = "پروژه ${template.name}",
                                        genre = template.genre
                                    )
                                )
                                try {
                                    val arr = JSONArray(template.defaultSectionsJson)
                                    for (i in 0 until arr.length()) {
                                        val obj = arr.getJSONObject(i)
                                        sectionRepo.insertSection(
                                            SectionEntity(
                                                projectId = projectId,
                                                type = obj.getString("type"),
                                                customTitle = obj.getString("title"),
                                                orderIndex = i
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                onNavigateToProject(projectId)
                            }
                        },
                        icon = Icons.Default.Check,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
