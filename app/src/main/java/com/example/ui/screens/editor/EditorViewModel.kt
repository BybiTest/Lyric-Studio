package com.example.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.LyricStudioApp
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.SectionEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class EditorUiState(
    val project: ProjectEntity? = null,
    val sections: List<SectionEntity> = emptyList(),
    val isFocusMode: Boolean = false,
    val isSaving: Boolean = false,
    val lastSavedTimestamp: Long = 0L,
    val totalWords: Int = 0,
    val totalLines: Int = 0,
    val fontSize: Float = 16f,
    val lineHeight: Float = 1.5f,
    val letterSpacing: Float = 0f
)

class EditorViewModel(private val projectId: Long) : ViewModel() {
    private val app = LyricStudioApp.instance
    private val projectRepo = app.projectRepository
    private val sectionRepo = app.sectionRepository
    private val streakRepo = app.streakRepository
    private val versionRepo = app.versionRepository
    private val prefs = app.preferencesManager

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var autosaveJob: Job? = null
    private var pendingSectionsToSave: List<SectionEntity>? = null

    init {
        loadProject()
        observePreferences()
    }

    private fun loadProject() {
        viewModelScope.launch {
            combine(
                projectRepo.getProject(projectId),
                sectionRepo.getSections(projectId)
            ) { proj, sections ->
                var words = 0
                var lines = 0
                for (s in sections) {
                    val text = s.content.trim()
                    if (text.isNotEmpty()) {
                        words += text.split("\\s+".toRegex()).size
                        lines += text.lines().size
                    }
                }
                _uiState.update { current ->
                    current.copy(
                        project = proj,
                        sections = sections,
                        totalWords = words,
                        totalLines = lines
                    )
                }
            }.collect()
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                prefs.fontSizeFlow,
                prefs.lineHeightFlow,
                prefs.letterSpacingFlow
            ) { font, line, letter ->
                _uiState.update {
                    it.copy(
                        fontSize = font,
                        lineHeight = line,
                        letterSpacing = letter
                    )
                }
            }.collect()
        }
    }

    fun updateSectionContent(sectionId: Long, newContent: String) {
        val currentSections = _uiState.value.sections
        val updated = currentSections.map {
            if (it.id == sectionId) it.copy(content = newContent) else it
        }
        _uiState.update { it.copy(sections = updated, isSaving = true) }
        pendingSectionsToSave = updated

        // Debounce 600ms as specified in prompt (§11)
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(600L)
            savePendingChanges()
        }
    }

    private suspend fun savePendingChanges() {
        val sectionsToSave = pendingSectionsToSave ?: return
        for (sec in sectionsToSave) {
            sectionRepo.updateSection(sec)
        }
        val currentProj = _uiState.value.project
        if (currentProj != null) {
            projectRepo.updateProject(currentProj)
        }
        streakRepo.recordWritingActivity(1)

        _uiState.update {
            it.copy(
                isSaving = false,
                lastSavedTimestamp = System.currentTimeMillis()
            )
        }
    }

    fun addSection(type: String, customTitle: String) {
        viewModelScope.launch {
            val maxOrder = _uiState.value.sections.maxOfOrNull { it.orderIndex } ?: -1
            val newSection = SectionEntity(
                projectId = projectId,
                type = type,
                customTitle = customTitle,
                orderIndex = maxOrder + 1
            )
            sectionRepo.insertSection(newSection)
        }
    }

    fun deleteSection(sectionId: Long) {
        viewModelScope.launch {
            sectionRepo.deleteSection(sectionId)
        }
    }

    fun moveSectionUp(sectionId: Long) {
        val sections = _uiState.value.sections.toMutableList()
        val index = sections.indexOfFirst { it.id == sectionId }
        if (index > 0) {
            val temp = sections[index]
            sections[index] = sections[index - 1]
            sections[index - 1] = temp
            viewModelScope.launch {
                sectionRepo.reorderSections(sections)
            }
        }
    }

    fun moveSectionDown(sectionId: Long) {
        val sections = _uiState.value.sections.toMutableList()
        val index = sections.indexOfFirst { it.id == sectionId }
        if (index >= 0 && index < sections.size - 1) {
            val temp = sections[index]
            sections[index] = sections[index + 1]
            sections[index + 1] = temp
            viewModelScope.launch {
                sectionRepo.reorderSections(sections)
            }
        }
    }

    fun updateProjectDetails(title: String, bpm: Int?, key: String, status: String) {
        viewModelScope.launch {
            val current = _uiState.value.project ?: return@launch
            val updated = current.copy(
                title = title.ifBlank { current.title },
                bpm = bpm,
                musicalKey = key,
                status = status
            )
            projectRepo.updateProject(updated)
            _uiState.update { it.copy(project = updated) }
        }
    }

    fun toggleFocusMode() {
        _uiState.update { it.copy(isFocusMode = !it.isFocusMode) }
    }

    fun createVersionSnapshot(label: String) {
        viewModelScope.launch {
            versionRepo.createSnapshot(
                projectId = projectId,
                label = label.ifBlank { "نسخه ${SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}" },
                sections = _uiState.value.sections
            )
        }
    }

    fun getFullLyricsText(): String {
        val proj = _uiState.value.project
        val sb = StringBuilder()
        if (proj != null) {
            sb.append(proj.title).append("\n")
            if (proj.genre.isNotBlank()) sb.append("سبک: ").append(proj.genre).append("\n")
            if (proj.bpm != null) sb.append("تمپو: ").append(proj.bpm).append(" BPM\n")
            if (proj.musicalKey.isNotBlank()) sb.append("گام: ").append(proj.musicalKey).append("\n")
            sb.append("─────────────────────\n\n")
        }
        for (sec in _uiState.value.sections) {
            val title = if (sec.customTitle.isNotBlank()) sec.customTitle else sec.type
            sb.append("[$title]\n")
            sb.append(sec.content).append("\n\n")
        }
        return sb.toString()
    }
}
