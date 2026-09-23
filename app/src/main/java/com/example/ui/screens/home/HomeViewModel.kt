package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.LyricStudioApp
import com.example.data.local.entity.IdeaEntity
import com.example.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val greetingRes: Int = com.example.R.string.greeting_morning,
    val latestProject: ProjectEntity? = null,
    val recentProjects: List<ProjectEntity> = emptyList(),
    val recentIdeas: List<IdeaEntity> = emptyList(),
    val totalProjects: Int = 0,
    val totalWords: Int = 0,
    val currentStreak: Int = 0,
    val isLoading: Boolean = false
)

class HomeViewModel : ViewModel() {
    private val app = LyricStudioApp.instance
    private val projectRepo = app.projectRepository
    private val sectionRepo = app.sectionRepository
    private val ideaRepo = app.ideaRepository
    private val streakRepo = app.streakRepository

    private val _uiState = MutableStateFlow(HomeUiState(greetingRes = calculateGreeting()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                projectRepo.latestProject,
                projectRepo.getRecentProjects(5),
                ideaRepo.getRecentIdeas(4),
                projectRepo.activeProjectsCount,
                streakRepo.allStreaks
            ) { latest, recentProjects, recentIdeas, totalProjects, streaks ->
                var totalWords = 0
                for (proj in recentProjects) {
                    val sections = sectionRepo.getSectionsDirect(proj.id)
                    for (s in sections) {
                        val text = s.content.trim()
                        if (text.isNotEmpty()) {
                            totalWords += text.split("\\s+".toRegex()).size
                        }
                    }
                }

                val currentStreak = calculateStreakDays(streaks.map { it.dateStr })

                HomeUiState(
                    greetingRes = calculateGreeting(),
                    latestProject = latest,
                    recentProjects = recentProjects,
                    recentIdeas = recentIdeas,
                    totalProjects = totalProjects,
                    totalWords = totalWords,
                    currentStreak = currentStreak,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun calculateGreeting(): Int {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> com.example.R.string.greeting_morning
            in 12..16 -> com.example.R.string.greeting_afternoon
            in 17..20 -> com.example.R.string.greeting_evening
            else -> com.example.R.string.greeting_night
        }
    }

    private fun calculateStreakDays(dateStrings: List<String>): Int {
        if (dateStrings.isEmpty()) return 0
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val today = Calendar.getInstance()
        var checkCal = Calendar.getInstance()
        var streak = 0

        val sortedSet = dateStrings.toSortedSet(Comparator.reverseOrder())
        val todayStr = sdf.format(today.time)
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayStr = sdf.format(yesterdayCal.time)

        if (!sortedSet.contains(todayStr) && !sortedSet.contains(yesterdayStr)) {
            return 0
        }

        if (sortedSet.contains(todayStr)) {
            checkCal = today
        } else {
            checkCal = yesterdayCal
        }

        while (true) {
            val dateStr = sdf.format(checkCal.time)
            if (sortedSet.contains(dateStr)) {
                streak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    fun createNewProject(title: String, genre: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = projectRepo.createProject(
                ProjectEntity(
                    title = title.ifBlank { "ترانه بدون عنوان" },
                    genre = genre
                )
            )
            // Add default sections: Verse 1, Chorus
            sectionRepo.insertSection(
                com.example.data.local.entity.SectionEntity(
                    projectId = id,
                    type = "Verse",
                    customTitle = "بند اول (Verse 1)",
                    orderIndex = 0
                )
            )
            sectionRepo.insertSection(
                com.example.data.local.entity.SectionEntity(
                    projectId = id,
                    type = "Chorus",
                    customTitle = "ترجیع‌بند (Chorus)",
                    orderIndex = 1
                )
            )
            onCreated(id)
        }
    }
}
