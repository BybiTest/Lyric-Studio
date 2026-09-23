package com.example.ui.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.LyricStudioApp
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.SectionEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProjectItemUi(
    val project: ProjectEntity,
    val wordCount: Int = 0,
    val lineCount: Int = 0
)

data class ProjectsUiState(
    val projects: List<ProjectItemUi> = emptyList(),
    val filteredProjects: List<ProjectItemUi> = emptyList(),
    val searchQuery: String = "",
    val selectedStatus: String = "All",
    val selectedGenre: String = "All",
    val sortOrder: SortOrder = SortOrder.DateDesc,
    val isGridView: Boolean = false,
    val isLoading: Boolean = false
)

enum class SortOrder {
    DateDesc,
    DateAsc,
    Title,
    WordCount
}

class ProjectsViewModel : ViewModel() {
    private val app = LyricStudioApp.instance
    private val projectRepo = app.projectRepository
    private val sectionRepo = app.sectionRepository
    private val prefs = app.preferencesManager

    private val _searchQuery = MutableStateFlow("")
    private val _selectedStatus = MutableStateFlow("All")
    private val _selectedGenre = MutableStateFlow("All")
    private val _sortOrder = MutableStateFlow(SortOrder.DateDesc)

    private val _uiState = MutableStateFlow(ProjectsUiState(isLoading = true))
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    private data class Filters(
        val query: String,
        val status: String,
        val genre: String,
        val sort: SortOrder,
        val isGrid: Boolean
    )

    init {
        viewModelScope.launch {
            val filtersFlow = combine(
                _searchQuery,
                _selectedStatus,
                _selectedGenre,
                _sortOrder,
                prefs.gridViewFlow
            ) { query, status, genre, sort, isGrid ->
                Filters(query, status, genre, sort, isGrid)
            }

            combine(
                projectRepo.activeProjects,
                filtersFlow
            ) { projects, filters ->
                val projectItems = projects.map { proj ->
                    val sections = sectionRepo.getSectionsDirect(proj.id)
                    var words = 0
                    var lines = 0
                    for (s in sections) {
                        val text = s.content.trim()
                        if (text.isNotEmpty()) {
                            words += text.split("\\s+".toRegex()).size
                            lines += text.lines().size
                        }
                    }
                    ProjectItemUi(proj, words, lines)
                }

                var filtered = projectItems.filter { item ->
                    val matchesQuery = filters.query.isBlank() ||
                            item.project.title.contains(filters.query, ignoreCase = true) ||
                            item.project.notes.contains(filters.query, ignoreCase = true) ||
                            item.project.topic.contains(filters.query, ignoreCase = true)

                    val matchesStatus = filters.status == "All" || item.project.status.equals(filters.status, ignoreCase = true)
                    val matchesGenre = filters.genre == "All" || item.project.genre.equals(filters.genre, ignoreCase = true)

                    matchesQuery && matchesStatus && matchesGenre
                }

                filtered = when (filters.sort) {
                    SortOrder.DateDesc -> filtered.sortedByDescending { it.project.lastEditedAt }
                    SortOrder.DateAsc -> filtered.sortedBy { it.project.lastEditedAt }
                    SortOrder.Title -> filtered.sortedBy { it.project.title.lowercase() }
                    SortOrder.WordCount -> filtered.sortedByDescending { it.wordCount }
                    else -> filtered
                }

                ProjectsUiState(
                    projects = projectItems,
                    filteredProjects = filtered,
                    searchQuery = filters.query,
                    selectedStatus = filters.status,
                    selectedGenre = filters.genre,
                    sortOrder = filters.sort,
                    isGridView = filters.isGrid,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onStatusSelected(status: String) {
        _selectedStatus.value = status
    }

    fun onGenreSelected(genre: String) {
        _selectedGenre.value = genre
    }

    fun onSortOrderChanged(sort: SortOrder) {
        _sortOrder.value = sort
    }

    fun toggleGridView() {
        viewModelScope.launch {
            prefs.setGridView(!_uiState.value.isGridView)
        }
    }

    fun createProject(title: String, genre: String, bpm: Int?, key: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val newProject = ProjectEntity(
                title = title.ifBlank { "ترانه جدید" },
                genre = genre,
                bpm = bpm,
                musicalKey = key
            )
            val id = projectRepo.createProject(newProject)
            sectionRepo.insertSection(
                SectionEntity(
                    projectId = id,
                    type = "Verse",
                    customTitle = "بند اول (Verse 1)",
                    orderIndex = 0
                )
            )
            sectionRepo.insertSection(
                SectionEntity(
                    projectId = id,
                    type = "Chorus",
                    customTitle = "ترجیع‌بند (Chorus)",
                    orderIndex = 1
                )
            )
            onCreated(id)
        }
    }

    fun duplicateProject(project: ProjectEntity) {
        viewModelScope.launch {
            val copy = project.copy(
                id = 0,
                title = "${project.title} (کپی)",
                createdAt = System.currentTimeMillis(),
                lastEditedAt = System.currentTimeMillis()
            )
            val newId = projectRepo.createProject(copy)
            val originalSections = sectionRepo.getSectionsDirect(project.id)
            val copiedSections = originalSections.map {
                it.copy(id = 0, projectId = newId, lastEditedAt = System.currentTimeMillis())
            }
            copiedSections.forEach { sectionRepo.insertSection(it) }
        }
    }

    fun moveToTrash(projectId: Long) {
        viewModelScope.launch {
            projectRepo.softDelete(projectId)
        }
    }
}
