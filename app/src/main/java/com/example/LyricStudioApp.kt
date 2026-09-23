package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.preferences.PreferencesManager
import com.example.data.repository.*

class LyricStudioApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var projectRepository: ProjectRepository
        private set

    lateinit var sectionRepository: SectionRepository
        private set

    lateinit var ideaRepository: IdeaRepository
        private set

    lateinit var punchlineRepository: PunchlineRepository
        private set

    lateinit var wordBankRepository: WordBankRepository
        private set

    lateinit var voiceRepository: VoiceRepository
        private set

    lateinit var versionRepository: VersionRepository
        private set

    lateinit var templateRepository: TemplateRepository
        private set

    lateinit var goalRepository: GoalRepository
        private set

    lateinit var streakRepository: StreakRepository
        private set

    lateinit var quickNoteRepository: QuickNoteRepository
        private set

    lateinit var searchRepository: SearchRepository
        private set

    lateinit var backupRepository: BackupRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        try {
            val sp = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val lang = sp.getString("language", "fa") ?: "fa"
            val locale = java.util.Locale(lang)
            java.util.Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)
            config.setLayoutDirection(locale)
        } catch (_: Exception) {}

        database = AppDatabase.getInstance(this)
        preferencesManager = PreferencesManager(this)

        projectRepository = ProjectRepository(database)
        sectionRepository = SectionRepository(database)
        ideaRepository = IdeaRepository(database)
        punchlineRepository = PunchlineRepository(database)
        wordBankRepository = WordBankRepository(database)
        voiceRepository = VoiceRepository(this, database)
        versionRepository = VersionRepository(database)
        templateRepository = TemplateRepository(database)
        goalRepository = GoalRepository(database)
        streakRepository = StreakRepository(database)
        quickNoteRepository = QuickNoteRepository(database)
        searchRepository = SearchRepository(database)
        backupRepository = BackupRepository(database)
    }

    companion object {
        lateinit var instance: LyricStudioApp
            private set
    }
}
