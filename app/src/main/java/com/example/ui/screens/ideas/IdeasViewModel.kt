package com.example.ui.screens.ideas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.LyricStudioApp
import com.example.data.local.entity.IdeaEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.SectionEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class IdeasUiState(
    val ideas: List<IdeaEntity> = emptyList(),
    val filteredIdeas: List<IdeaEntity> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val onlyFavorites: Boolean = false,
    val isLoading: Boolean = false
)

class IdeasViewModel : ViewModel() {
    private val app = LyricStudioApp.instance
    private val ideaRepo = app.ideaRepository
    private val projectRepo = app.projectRepository
    private val sectionRepo = app.sectionRepository

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")
    private val _onlyFavorites = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(IdeasUiState(isLoading = true))
    val uiState: StateFlow<IdeasUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                ideaRepo.allIdeas,
                _searchQuery,
                _selectedCategory,
                _onlyFavorites
            ) { ideas, query, cat, favOnly ->
                val filtered = ideas.filter { idea ->
                    val matchesQuery = query.isBlank() ||
                            idea.title.contains(query, ignoreCase = true) ||
                            idea.content.contains(query, ignoreCase = true)
                    val matchesCategory = cat == "All" || idea.category.equals(cat, ignoreCase = true)
                    val matchesFav = !favOnly || idea.isFavorite

                    matchesQuery && matchesCategory && matchesFav
                }

                IdeasUiState(
                    ideas = ideas,
                    filteredIdeas = filtered,
                    selectedCategory = cat,
                    searchQuery = query,
                    onlyFavorites = favOnly,
                    isLoading = false
                )
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: String) {
        _selectedCategory.value = category
    }

    fun toggleFavoritesFilter() {
        _onlyFavorites.value = !_onlyFavorites.value
    }

    fun addIdea(title: String, content: String, category: String) {
        viewModelScope.launch {
            ideaRepo.insertIdea(
                IdeaEntity(
                    title = title.ifBlank { "ایده جدید" },
                    content = content,
                    category = category
                )
            )
        }
    }

    fun updateIdea(idea: IdeaEntity) {
        viewModelScope.launch {
            ideaRepo.updateIdea(idea)
        }
    }

    fun toggleFavorite(idea: IdeaEntity) {
        viewModelScope.launch {
            ideaRepo.updateIdea(idea.copy(isFavorite = !idea.isFavorite))
        }
    }

    fun deleteIdea(id: Long) {
        viewModelScope.launch {
            ideaRepo.deleteIdea(id)
        }
    }

    fun convertIdeaToProject(idea: IdeaEntity, onProjectCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val projectId = projectRepo.createProject(
                ProjectEntity(
                    title = idea.title,
                    topic = idea.title,
                    genre = "Rap",
                    status = "Idea"
                )
            )
            sectionRepo.insertSection(
                SectionEntity(
                    projectId = projectId,
                    type = "Verse",
                    customTitle = "ایده اولیه",
                    content = idea.content,
                    orderIndex = 0
                )
            )
            onProjectCreated(projectId)
        }
    }
}
