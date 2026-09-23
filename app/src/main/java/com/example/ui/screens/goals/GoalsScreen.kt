package com.example.ui.screens.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.LyricStudioApp
import com.example.R
import com.example.data.local.entity.GoalEntity
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    onNavigateBack: () -> Unit
) {
    val app = LyricStudioApp.instance
    val repo = app.goalRepository
    val scope = rememberCoroutineScope()

    val currentGoal by repo.goals.collectAsState(initial = null)

    var wordsDaily by remember { mutableFloatStateOf(200f) }
    var linesDaily by remember { mutableFloatStateOf(20f) }
    var sessionsWeekly by remember { mutableFloatStateOf(5f) }
    var projectsMonthly by remember { mutableFloatStateOf(2f) }
    var hasSavedFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(currentGoal) {
        currentGoal?.let {
            wordsDaily = it.wordsDailyGoal.toFloat()
            linesDaily = it.linesDailyGoal.toFloat()
            sessionsWeekly = it.sessionsWeeklyGoal.toFloat()
            projectsMonthly = it.projectsMonthlyGoal.toFloat()
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
                        text = stringResource(R.string.goals_title),
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.words_goal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${wordsDaily.toInt()} کلمه در روز",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = wordsDaily,
                        onValueChange = { wordsDaily = it },
                        valueRange = 50f..1000f,
                        steps = 18
                    )
                }
            }

            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.lines_goal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${linesDaily.toInt()} خط / بیت در روز",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = linesDaily,
                        onValueChange = { linesDaily = it },
                        valueRange = 4f..80f,
                        steps = 18
                    )
                }
            }

            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.projects_goal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${projectsMonthly.toInt()} ترانه در ماه",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = projectsMonthly,
                        onValueChange = { projectsMonthly = it },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
            }

            item {
                StudioButton(
                    text = if (hasSavedFeedback) "تنظیمات اهداف ذخیره شد ✓" else stringResource(R.string.save),
                    onClick = {
                        scope.launch {
                            repo.updateGoals(
                                GoalEntity(
                                    id = 1,
                                    wordsDailyGoal = wordsDaily.toInt(),
                                    linesDailyGoal = linesDaily.toInt(),
                                    sessionsWeeklyGoal = sessionsWeekly.toInt(),
                                    projectsMonthlyGoal = projectsMonthly.toInt()
                                )
                            )
                            hasSavedFeedback = true
                        }
                    },
                    icon = Icons.Default.Save,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
