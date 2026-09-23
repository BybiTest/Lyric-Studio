package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.preferences.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ProjectRepository(private val db: AppDatabase) {
    val activeProjects: Flow<List<ProjectEntity>> = db.projectDao().getAllActiveProjects()
    val vaultProjects: Flow<List<ProjectEntity>> = db.projectDao().getVaultProjects()
    val trashProjects: Flow<List<ProjectEntity>> = db.projectDao().getTrashProjects()
    val latestProject: Flow<ProjectEntity?> = db.projectDao().getLatestProject()
    val activeProjectsCount: Flow<Int> = db.projectDao().getActiveProjectsCount()

    fun getRecentProjects(limit: Int = 5): Flow<List<ProjectEntity>> =
        db.projectDao().getRecentProjects(limit)

    fun getProject(id: Long): Flow<ProjectEntity?> =
        db.projectDao().getProjectById(id)

    suspend fun getProjectDirect(id: Long): ProjectEntity? = withContext(Dispatchers.IO) {
        db.projectDao().getProjectByIdDirect(id)
    }

    suspend fun createProject(project: ProjectEntity): Long = withContext(Dispatchers.IO) {
        db.projectDao().insertProject(project)
    }

    suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        db.projectDao().updateProject(project.copy(lastEditedAt = System.currentTimeMillis()))
    }

    suspend fun softDelete(id: Long) = withContext(Dispatchers.IO) {
        db.projectDao().softDelete(id)
    }

    suspend fun restoreProject(id: Long) = withContext(Dispatchers.IO) {
        db.projectDao().restoreProject(id)
    }

    suspend fun deletePermanently(id: Long) = withContext(Dispatchers.IO) {
        db.sectionDao().deleteSectionsForProject(id)
        db.projectDao().deletePermanently(id)
    }

    suspend fun emptyTrash() = withContext(Dispatchers.IO) {
        db.projectDao().emptyTrash()
    }
}

class SectionRepository(private val db: AppDatabase) {
    fun getSections(projectId: Long): Flow<List<SectionEntity>> =
        db.sectionDao().getSectionsForProject(projectId)

    suspend fun getSectionsDirect(projectId: Long): List<SectionEntity> = withContext(Dispatchers.IO) {
        db.sectionDao().getSectionsForProjectDirect(projectId)
    }

    suspend fun insertSection(section: SectionEntity): Long = withContext(Dispatchers.IO) {
        db.sectionDao().insertSection(section)
    }

    suspend fun updateSection(section: SectionEntity) = withContext(Dispatchers.IO) {
        db.sectionDao().updateSection(section.copy(lastEditedAt = System.currentTimeMillis()))
    }

    suspend fun deleteSection(id: Long) = withContext(Dispatchers.IO) {
        db.sectionDao().deleteSection(id)
    }

    suspend fun deleteSectionsForProject(projectId: Long) = withContext(Dispatchers.IO) {
        db.sectionDao().deleteSectionsForProject(projectId)
    }

    suspend fun reorderSections(sections: List<SectionEntity>) = withContext(Dispatchers.IO) {
        val updated = sections.mapIndexed { index, s -> s.copy(orderIndex = index) }
        db.sectionDao().insertSections(updated)
    }
}

class IdeaRepository(private val db: AppDatabase) {
    val allIdeas: Flow<List<IdeaEntity>> = db.ideaDao().getAllIdeas()

    fun getRecentIdeas(limit: Int = 5): Flow<List<IdeaEntity>> =
        db.ideaDao().getRecentIdeas(limit)

    suspend fun insertIdea(idea: IdeaEntity): Long = withContext(Dispatchers.IO) {
        db.ideaDao().insertIdea(idea)
    }

    suspend fun updateIdea(idea: IdeaEntity) = withContext(Dispatchers.IO) {
        db.ideaDao().updateIdea(idea.copy(lastEditedAt = System.currentTimeMillis()))
    }

    suspend fun deleteIdea(id: Long) = withContext(Dispatchers.IO) {
        db.ideaDao().deleteIdea(id)
    }
}

class PunchlineRepository(private val db: AppDatabase) {
    val allPunchlines: Flow<List<PunchlineEntity>> = db.punchlineDao().getAllPunchlines()

    suspend fun insertPunchline(punchline: PunchlineEntity): Long = withContext(Dispatchers.IO) {
        db.punchlineDao().insertPunchline(punchline)
    }

    suspend fun updatePunchline(punchline: PunchlineEntity) = withContext(Dispatchers.IO) {
        db.punchlineDao().updatePunchline(punchline)
    }

    suspend fun deletePunchline(id: Long) = withContext(Dispatchers.IO) {
        db.punchlineDao().deletePunchline(id)
    }
}

class WordBankRepository(private val db: AppDatabase) {
    val allWords: Flow<List<WordBankEntity>> = db.wordBankDao().getAllWords()

    suspend fun insertWord(word: WordBankEntity): Long = withContext(Dispatchers.IO) {
        db.wordBankDao().insertWord(word)
    }

    suspend fun updateWord(word: WordBankEntity) = withContext(Dispatchers.IO) {
        db.wordBankDao().updateWord(word)
    }

    suspend fun deleteWord(id: Long) = withContext(Dispatchers.IO) {
        db.wordBankDao().deleteWord(id)
    }
}

class VoiceRepository(private val context: Context, private val db: AppDatabase) {
    val allVoiceMemos: Flow<List<VoiceMemoEntity>> = db.voiceMemoDao().getAllVoiceMemos()

    fun getVoiceMemosForProject(projectId: Long): Flow<List<VoiceMemoEntity>> =
        db.voiceMemoDao().getVoiceMemosForProject(projectId)

    suspend fun saveVoiceMemo(memo: VoiceMemoEntity): Long = withContext(Dispatchers.IO) {
        db.voiceMemoDao().insertVoiceMemo(memo)
    }

    suspend fun renameVoiceMemo(id: Long, title: String) = withContext(Dispatchers.IO) {
        db.voiceMemoDao().renameVoiceMemo(id, title)
    }

    suspend fun deleteVoiceMemo(id: Long) = withContext(Dispatchers.IO) {
        val memo = db.voiceMemoDao().getVoiceMemoById(id)
        if (memo != null) {
            val file = File(memo.filePath)
            if (file.exists()) {
                file.delete()
            }
            db.voiceMemoDao().deleteVoiceMemo(id)
        }
    }
}

class VersionRepository(private val db: AppDatabase) {
    fun getVersionsForProject(projectId: Long): Flow<List<VersionEntity>> =
        db.versionDao().getVersionsForProject(projectId)

    suspend fun createSnapshot(
        projectId: Long,
        label: String,
        sections: List<SectionEntity>
    ): Long = withContext(Dispatchers.IO) {
        val array = JSONArray()
        var totalWords = 0
        var totalLines = 0
        for (sec in sections) {
            val obj = JSONObject()
            obj.put("type", sec.type)
            obj.put("customTitle", sec.customTitle)
            obj.put("content", sec.content)
            obj.put("orderIndex", sec.orderIndex)
            array.put(obj)

            val text = sec.content.trim()
            if (text.isNotEmpty()) {
                totalWords += text.split("\\s+".toRegex()).size
                totalLines += text.lines().size
            }
        }
        val entity = VersionEntity(
            projectId = projectId,
            label = label,
            fullSnapshotJson = array.toString(),
            wordCount = totalWords,
            lineCount = totalLines,
            createdAt = System.currentTimeMillis()
        )
        db.versionDao().insertVersion(entity)
    }

    suspend fun deleteVersion(id: Long) = withContext(Dispatchers.IO) {
        db.versionDao().deleteVersion(id)
    }
}

class TemplateRepository(private val db: AppDatabase) {
    val allTemplates: Flow<List<TemplateEntity>> = db.templateDao().getAllTemplates()

    suspend fun insertTemplate(template: TemplateEntity): Long = withContext(Dispatchers.IO) {
        db.templateDao().insertTemplate(template)
    }

    suspend fun deleteTemplate(id: Long) = withContext(Dispatchers.IO) {
        db.templateDao().deleteTemplate(id)
    }
}

class GoalRepository(private val db: AppDatabase) {
    val goals: Flow<GoalEntity?> = db.goalDao().getGoals()

    suspend fun updateGoals(goals: GoalEntity) = withContext(Dispatchers.IO) {
        db.goalDao().updateGoals(goals)
    }
}

class StreakRepository(private val db: AppDatabase) {
    val allStreaks: Flow<List<StreakEntity>> = db.streakDao().getAllStreaks()
    val totalActiveDays: Flow<Int> = db.streakDao().getTotalActiveDays()

    suspend fun recordWritingActivity(wordsWritten: Int) = withContext(Dispatchers.IO) {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val existing = db.streakDao().getStreakForDate(todayStr)
        val newWords = (existing?.wordsWritten ?: 0) + wordsWritten
        db.streakDao().recordActivity(
            StreakEntity(
                dateStr = todayStr,
                wordsWritten = newWords,
                activeDurationMinutes = (existing?.activeDurationMinutes ?: 0) + 1,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}

class QuickNoteRepository(private val db: AppDatabase) {
    val quickNote: Flow<QuickNoteEntity?> = db.quickNoteDao().getQuickNote()

    suspend fun saveNote(content: String) = withContext(Dispatchers.IO) {
        db.quickNoteDao().saveQuickNote(
            QuickNoteEntity(id = 1, content = content, lastEditedAt = System.currentTimeMillis())
        )
    }
}

class SearchRepository(private val db: AppDatabase) {
    fun searchProjects(query: String): Flow<List<ProjectEntity>> = db.searchDao().searchProjects(query)
    fun searchSections(query: String): Flow<List<SectionEntity>> = db.searchDao().searchSections(query)
    fun searchIdeas(query: String): Flow<List<IdeaEntity>> = db.searchDao().searchIdeas(query)
    fun searchPunchlines(query: String): Flow<List<PunchlineEntity>> = db.searchDao().searchPunchlines(query)
    fun searchWords(query: String): Flow<List<WordBankEntity>> = db.searchDao().searchWords(query)
}

class BackupRepository(private val db: AppDatabase) {
    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", "1.0.0")
        root.put("timestamp", System.currentTimeMillis())

        val projects = db.projectDao().getAllProjectsForBackup()
        val projectsArray = JSONArray()
        for (p in projects) {
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("title", p.title)
            pObj.put("genre", p.genre)
            pObj.put("subgenre", p.subgenre)
            pObj.put("bpm", p.bpm ?: JSONObject.NULL)
            pObj.put("musicalKey", p.musicalKey)
            pObj.put("mood", p.mood)
            pObj.put("topic", p.topic)
            pObj.put("notes", p.notes)
            pObj.put("status", p.status)
            pObj.put("createdAt", p.createdAt)
            pObj.put("lastEditedAt", p.lastEditedAt)
            pObj.put("isArchived", p.isArchived)
            pObj.put("isDeleted", p.isDeleted)
            pObj.put("isVault", p.isVault)
            projectsArray.put(pObj)
        }
        root.put("projects", projectsArray)

        val sections = db.sectionDao().getAllSectionsForBackup()
        val sectionsArray = JSONArray()
        for (s in sections) {
            val sObj = JSONObject()
            sObj.put("id", s.id)
            sObj.put("projectId", s.projectId)
            sObj.put("type", s.type)
            sObj.put("customTitle", s.customTitle)
            sObj.put("content", s.content)
            sObj.put("orderIndex", s.orderIndex)
            sObj.put("lastEditedAt", s.lastEditedAt)
            sectionsArray.put(sObj)
        }
        root.put("sections", sectionsArray)

        val ideas = db.ideaDao().getAllIdeasForBackup()
        val ideasArray = JSONArray()
        for (i in ideas) {
            val iObj = JSONObject()
            iObj.put("id", i.id)
            iObj.put("title", i.title)
            iObj.put("content", i.content)
            iObj.put("category", i.category)
            iObj.put("isFavorite", i.isFavorite)
            iObj.put("createdAt", i.createdAt)
            ideasArray.put(iObj)
        }
        root.put("ideas", ideasArray)

        val punchlines = db.punchlineDao().getAllPunchlinesForBackup()
        val puncArray = JSONArray()
        for (p in punchlines) {
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("text", p.text)
            pObj.put("type", p.type)
            pObj.put("category", p.category)
            pObj.put("isFavorite", p.isFavorite)
            pObj.put("createdAt", p.createdAt)
            puncArray.put(pObj)
        }
        root.put("punchlines", puncArray)

        val words = db.wordBankDao().getAllWordsForBackup()
        val wordsArray = JSONArray()
        for (w in words) {
            val wObj = JSONObject()
            wObj.put("id", w.id)
            wObj.put("word", w.word)
            wObj.put("meaning", w.meaning)
            wObj.put("category", w.category)
            wObj.put("isFavorite", w.isFavorite)
            wObj.put("createdAt", w.createdAt)
            wordsArray.put(wObj)
        }
        root.put("word_bank", wordsArray)

        root.toString(2)
    }

    suspend fun restoreBackupJson(jsonStr: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonStr)
            if (root.has("projects")) {
                val array = root.getJSONArray("projects")
                val projects = mutableListOf<ProjectEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    projects.add(
                        ProjectEntity(
                            id = obj.optLong("id", 0),
                            title = obj.getString("title"),
                            genre = obj.optString("genre", "Rap"),
                            subgenre = obj.optString("subgenre", ""),
                            bpm = if (obj.isNull("bpm")) null else obj.optInt("bpm"),
                            musicalKey = obj.optString("musicalKey", ""),
                            mood = obj.optString("mood", ""),
                            topic = obj.optString("topic", ""),
                            notes = obj.optString("notes", ""),
                            status = obj.optString("status", "Writing"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            lastEditedAt = obj.optLong("lastEditedAt", System.currentTimeMillis()),
                            isArchived = obj.optBoolean("isArchived", false),
                            isDeleted = obj.optBoolean("isDeleted", false),
                            isVault = obj.optBoolean("isVault", false)
                        )
                    )
                }
                db.projectDao().insertAll(projects)
            }

            if (root.has("sections")) {
                val array = root.getJSONArray("sections")
                val sections = mutableListOf<SectionEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    sections.add(
                        SectionEntity(
                            id = obj.optLong("id", 0),
                            projectId = obj.getLong("projectId"),
                            type = obj.optString("type", "Verse"),
                            customTitle = obj.optString("customTitle", ""),
                            content = obj.optString("content", ""),
                            orderIndex = obj.optInt("orderIndex", 0),
                            lastEditedAt = obj.optLong("lastEditedAt", System.currentTimeMillis())
                        )
                    )
                }
                db.sectionDao().insertSections(sections)
            }

            if (root.has("ideas")) {
                val array = root.getJSONArray("ideas")
                val ideas = mutableListOf<IdeaEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    ideas.add(
                        IdeaEntity(
                            id = obj.optLong("id", 0),
                            title = obj.getString("title"),
                            content = obj.optString("content", ""),
                            category = obj.optString("category", "SongIdea"),
                            isFavorite = obj.optBoolean("isFavorite", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            lastEditedAt = obj.optLong("lastEditedAt", System.currentTimeMillis())
                        )
                    )
                }
                db.ideaDao().insertAll(ideas)
            }

            if (root.has("punchlines")) {
                val array = root.getJSONArray("punchlines")
                val punchlines = mutableListOf<PunchlineEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    punchlines.add(
                        PunchlineEntity(
                            id = obj.optLong("id", 0),
                            text = obj.getString("text"),
                            type = obj.optString("type", "Punchline"),
                            category = obj.optString("category", "General"),
                            isFavorite = obj.optBoolean("isFavorite", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                db.punchlineDao().insertAll(punchlines)
            }

            if (root.has("word_bank")) {
                val array = root.getJSONArray("word_bank")
                val words = mutableListOf<WordBankEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    words.add(
                        WordBankEntity(
                            id = obj.optLong("id", 0),
                            word = obj.getString("word"),
                            meaning = obj.optString("meaning", ""),
                            category = obj.optString("category", "Rhyme"),
                            isFavorite = obj.optBoolean("isFavorite", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                db.wordBankDao().insertAll(words)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
