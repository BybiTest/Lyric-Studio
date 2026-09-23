package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "projects",
    indices = [
        Index(value = ["isDeleted", "isArchived", "lastEditedAt"]),
        Index(value = ["isVault"])
    ]
)
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val genre: String = "Rap",
    val subgenre: String = "",
    val bpm: Int? = null,
    val musicalKey: String = "",
    val mood: String = "",
    val topic: String = "",
    val tags: List<String> = emptyList(),
    val status: String = "Writing",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastEditedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val isVault: Boolean = false
)

@Entity(
    tableName = "sections",
    indices = [
        Index(value = ["projectId", "orderIndex"])
    ]
)
data class SectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val type: String = "Verse",
    val customTitle: String = "",
    val content: String = "",
    val orderIndex: Int = 0,
    val lastEditedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ideas",
    indices = [
        Index(value = ["isFavorite", "createdAt"])
    ]
)
data class IdeaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String = "",
    val category: String = "SongIdea",
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastEditedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "punchlines",
    indices = [
        Index(value = ["isFavorite", "createdAt"])
    ]
)
data class PunchlineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val type: String = "Punchline",
    val category: String = "General",
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "word_bank",
    indices = [
        Index(value = ["word", "isFavorite"])
    ]
)
data class WordBankEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val meaning: String = "",
    val category: String = "Rhyme",
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "voice_memos",
    indices = [
        Index(value = ["projectId", "createdAt"])
    ]
)
data class VoiceMemoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long? = null,
    val title: String,
    val filePath: String,
    val durationMs: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "version_history",
    indices = [
        Index(value = ["projectId", "createdAt"])
    ]
)
data class VersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val label: String,
    val fullSnapshotJson: String,
    val wordCount: Int = 0,
    val lineCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val genre: String,
    val description: String,
    val defaultSectionsJson: String,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey
    val id: Int = 1,
    val wordsDailyGoal: Int = 200,
    val linesDailyGoal: Int = 20,
    val sessionsWeeklyGoal: Int = 5,
    val projectsMonthlyGoal: Int = 2,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey
    val dateStr: String, // Format: YYYY-MM-DD
    val wordsWritten: Int = 0,
    val activeDurationMinutes: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quick_notes")
data class QuickNoteEntity(
    @PrimaryKey
    val id: Int = 1,
    val content: String = "",
    val lastEditedAt: Long = System.currentTimeMillis()
)
