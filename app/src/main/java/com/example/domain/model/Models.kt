package com.example.domain.model

enum class ProjectStatus(val enLabel: String, val faLabel: String) {
    Idea("Idea", "ایده"),
    Writing("Writing", "در حال نوشتن"),
    Editing("Editing", "در حال ویرایش"),
    Ready("Ready", "آماده"),
    Completed("Completed", "تکمیل‌شده"),
    Archived("Archived", "بایگانی‌شده");

    companion object {
        fun fromString(value: String): ProjectStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Writing
        }
    }
}

enum class SectionType(val enName: String, val faName: String) {
    Intro("Intro", "مقدمه"),
    Verse("Verse", "بند اصلی"),
    Chorus("Chorus", "ترجیع‌بند"),
    Hook("Hook", "هوک"),
    PreChorus("Pre-Chorus", "پیش‌ترجیع‌بند"),
    Bridge("Bridge", "پل ملودی"),
    Outro("Outro", "پایان"),
    Poem("Poem", "بیت / شعر"),
    Text("Text", "متن آزاد"),
    Note("Note", "یادداشت فنی"),
    Idea("Idea", "ایده"),
    Custom("Custom", "سفارشی");

    companion object {
        fun fromString(value: String): SectionType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Verse
        }
    }
}

enum class IdeaCategory(val enLabel: String, val faLabel: String) {
    SongIdea("Song Idea", "ایده ترانه"),
    HookIdea("Hook Idea", "ایده هوک"),
    VerseIdea("Verse Idea", "ایده ورس"),
    Topic("Topic", "موضوع و مفهوم"),
    Concept("Concept", "کانسپت کلی"),
    Freestyle("Freestyle", "فری‌استایل"),
    Poetry("Poetry", "شعر و ادب"),
    RandomIdea("Random Idea", "ایده ناگهانی"),
    PersonalWriting("Personal", "دست‌نوشته شخصی");

    companion object {
        fun fromString(value: String): IdeaCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SongIdea
        }
    }
}

enum class PunchlineType(val enLabel: String, val faLabel: String) {
    Punchline("Punchline", "پانچ‌لاین"),
    Bar("Bar", "بیت سنگین (Bar)"),
    Wordplay("Wordplay", "بازی با کلمات"),
    Metaphor("Metaphor", "تشبیه و استعاره"),
    OneLiner("One-liner", "تک‌خطی کوبنده"),
    RapIdea("Rap Idea", "ایده رپ");

    companion object {
        fun fromString(value: String): PunchlineType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Punchline
        }
    }
}

enum class WritingStyle(val enLabel: String, val faLabel: String) {
    Rap("Rap", "رپ"),
    HipHop("Hip-Hop", "هیپ‌هاپ"),
    Pop("Pop", "پاپ"),
    RnB("R&B", "آر اند بی"),
    Rock("Rock", "راک"),
    Poetry("Poetry", "شعر کلاسیک و نو"),
    Lyrics("Lyrics", "ترانه استاندارد"),
    SongConcept("Song Concept", "کانسپت آهنگ"),
    Hook("Hook", "هوک و ملودی"),
    Punchline("Punchline", "پانچ‌لاین"),
    Freestyle("Freestyle", "فری‌استایل"),
    VoiceIdeas("Voice Ideas", "زمزمه صوتی"),
    PersonalWriting("Personal Writing", "نوشته شخصی"),
    GeneralText("General Text", "متن آزاد");

    companion object {
        fun fromString(value: String): WritingStyle {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Rap
        }
    }
}

enum class StudioTheme(val id: String, val enName: String, val faName: String) {
    MidnightMetallic("midnight", "Midnight Metallic", "مشکی متالیک"),
    PurpleNight("purple", "Purple Night", "شب بنفش"),
    DeepOcean("ocean", "Deep Ocean", "اقیانوس عمیق"),
    Emerald("emerald", "Emerald", "زمردی"),
    Crimson("crimson", "Crimson", "یاقوتی"),
    Golden("golden", "Golden", "زرین لوکس"),
    Neon("neon", "Neon", "نئون استودیو"),
    Light("light", "Light Studio", "استودیو روشن");

    companion object {
        fun fromId(id: String): StudioTheme {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MidnightMetallic
        }
    }
}
