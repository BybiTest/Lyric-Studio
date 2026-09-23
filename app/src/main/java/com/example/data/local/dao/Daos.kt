package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE isDeleted = 0 AND isVault = 0 ORDER BY lastEditedAt DESC")
    fun getAllActiveProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE isDeleted = 0 AND isVault = 1 ORDER BY lastEditedAt DESC")
    fun getVaultProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getTrashProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: Long): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectByIdDirect(id: Long): ProjectEntity?

    @Query("SELECT * FROM projects WHERE isDeleted = 0 AND isVault = 0 ORDER BY lastEditedAt DESC LIMIT 1")
    fun getLatestProject(): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE isDeleted = 0 AND isVault = 0 ORDER BY lastEditedAt DESC LIMIT :limit")
    fun getRecentProjects(limit: Int): Flow<List<ProjectEntity>>

    @Query("SELECT COUNT(*) FROM projects WHERE isDeleted = 0")
    fun getActiveProjectsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("UPDATE projects SET isDeleted = 1, deletedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE projects SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreProject(id: Long)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("DELETE FROM projects WHERE isDeleted = 1")
    suspend fun emptyTrash()

    @Query("SELECT * FROM projects")
    suspend fun getAllProjectsForBackup(): List<ProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<ProjectEntity>)
}

@Dao
interface SectionDao {
    @Query("SELECT * FROM sections WHERE projectId = :projectId ORDER BY orderIndex ASC")
    fun getSectionsForProject(projectId: Long): Flow<List<SectionEntity>>

    @Query("SELECT * FROM sections WHERE projectId = :projectId ORDER BY orderIndex ASC")
    suspend fun getSectionsForProjectDirect(projectId: Long): List<SectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<SectionEntity>)

    @Update
    suspend fun updateSection(section: SectionEntity)

    @Query("DELETE FROM sections WHERE id = :id")
    suspend fun deleteSection(id: Long)

    @Query("DELETE FROM sections WHERE projectId = :projectId")
    suspend fun deleteSectionsForProject(projectId: Long)

    @Query("SELECT * FROM sections")
    suspend fun getAllSectionsForBackup(): List<SectionEntity>
}

@Dao
interface IdeaDao {
    @Query("SELECT * FROM ideas ORDER BY createdAt DESC")
    fun getAllIdeas(): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentIdeas(limit: Int): Flow<List<IdeaEntity>>

    @Query("SELECT * FROM ideas WHERE id = :id")
    fun getIdeaById(id: Long): Flow<IdeaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: IdeaEntity): Long

    @Update
    suspend fun updateIdea(idea: IdeaEntity)

    @Query("DELETE FROM ideas WHERE id = :id")
    suspend fun deleteIdea(id: Long)

    @Query("SELECT * FROM ideas")
    suspend fun getAllIdeasForBackup(): List<IdeaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ideas: List<IdeaEntity>)
}

@Dao
interface PunchlineDao {
    @Query("SELECT * FROM punchlines ORDER BY createdAt DESC")
    fun getAllPunchlines(): Flow<List<PunchlineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPunchline(punchline: PunchlineEntity): Long

    @Update
    suspend fun updatePunchline(punchline: PunchlineEntity)

    @Query("DELETE FROM punchlines WHERE id = :id")
    suspend fun deletePunchline(id: Long)

    @Query("SELECT * FROM punchlines")
    suspend fun getAllPunchlinesForBackup(): List<PunchlineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(punchlines: List<PunchlineEntity>)
}

@Dao
interface WordBankDao {
    @Query("SELECT * FROM word_bank ORDER BY word ASC")
    fun getAllWords(): Flow<List<WordBankEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordBankEntity): Long

    @Update
    suspend fun updateWord(word: WordBankEntity)

    @Query("DELETE FROM word_bank WHERE id = :id")
    suspend fun deleteWord(id: Long)

    @Query("SELECT * FROM word_bank")
    suspend fun getAllWordsForBackup(): List<WordBankEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordBankEntity>)
}

@Dao
interface VoiceMemoDao {
    @Query("SELECT * FROM voice_memos ORDER BY createdAt DESC")
    fun getAllVoiceMemos(): Flow<List<VoiceMemoEntity>>

    @Query("SELECT * FROM voice_memos WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getVoiceMemosForProject(projectId: Long): Flow<List<VoiceMemoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceMemo(memo: VoiceMemoEntity): Long

    @Query("UPDATE voice_memos SET title = :title WHERE id = :id")
    suspend fun renameVoiceMemo(id: Long, title: String)

    @Query("DELETE FROM voice_memos WHERE id = :id")
    suspend fun deleteVoiceMemo(id: Long)

    @Query("SELECT * FROM voice_memos WHERE id = :id")
    suspend fun getVoiceMemoById(id: Long): VoiceMemoEntity?

    @Query("SELECT * FROM voice_memos")
    suspend fun getAllVoiceMemosForBackup(): List<VoiceMemoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memos: List<VoiceMemoEntity>)
}

@Dao
interface VersionDao {
    @Query("SELECT * FROM version_history WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getVersionsForProject(projectId: Long): Flow<List<VersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: VersionEntity): Long

    @Query("DELETE FROM version_history WHERE id = :id")
    suspend fun deleteVersion(id: Long)

    @Query("SELECT * FROM version_history")
    suspend fun getAllVersionsForBackup(): List<VersionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(versions: List<VersionEntity>)
}

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates ORDER BY isCustom DESC, name ASC")
    fun getAllTemplates(): Flow<List<TemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateEntity): Long

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun deleteTemplate(id: Long)

    @Query("SELECT COUNT(*) FROM templates")
    suspend fun getCount(): Int
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE id = 1")
    fun getGoals(): Flow<GoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateGoals(goal: GoalEntity)
}

@Dao
interface StreakDao {
    @Query("SELECT * FROM streaks ORDER BY dateStr DESC")
    fun getAllStreaks(): Flow<List<StreakEntity>>

    @Query("SELECT * FROM streaks WHERE dateStr = :dateStr")
    suspend fun getStreakForDate(dateStr: String): StreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordActivity(streak: StreakEntity)

    @Query("SELECT COUNT(*) FROM streaks")
    fun getTotalActiveDays(): Flow<Int>
}

@Dao
interface QuickNoteDao {
    @Query("SELECT * FROM quick_notes WHERE id = 1")
    fun getQuickNote(): Flow<QuickNoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuickNote(note: QuickNoteEntity)
}

@Dao
interface SearchDao {
    @Query("""
        SELECT * FROM projects 
        WHERE isDeleted = 0 AND (
            title LIKE '%' || :query || '%' OR 
            notes LIKE '%' || :query || '%' OR 
            genre LIKE '%' || :query || '%' OR 
            topic LIKE '%' || :query || '%'
        )
        ORDER BY lastEditedAt DESC
    """)
    fun searchProjects(query: String): Flow<List<ProjectEntity>>

    @Query("""
        SELECT * FROM sections 
        WHERE content LIKE '%' || :query || '%' OR 
              customTitle LIKE '%' || :query || '%'
        ORDER BY lastEditedAt DESC
    """)
    fun searchSections(query: String): Flow<List<SectionEntity>>

    @Query("""
        SELECT * FROM ideas 
        WHERE title LIKE '%' || :query || '%' OR 
              content LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchIdeas(query: String): Flow<List<IdeaEntity>>

    @Query("""
        SELECT * FROM punchlines 
        WHERE text LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun searchPunchlines(query: String): Flow<List<PunchlineEntity>>

    @Query("""
        SELECT * FROM word_bank 
        WHERE word LIKE '%' || :query || '%' OR 
              meaning LIKE '%' || :query || '%'
        ORDER BY word ASC
    """)
    fun searchWords(query: String): Flow<List<WordBankEntity>>
}
