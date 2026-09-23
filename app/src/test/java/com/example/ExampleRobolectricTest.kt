package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.ProjectStatus
import com.example.domain.model.SectionType
import com.example.domain.model.StudioTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertNotNull(appName)
  }

  @Test
  fun `verify domain models`() {
    assertEquals(ProjectStatus.Writing, ProjectStatus.fromString("Writing"))
    assertEquals(SectionType.Verse, SectionType.fromString("Verse"))
    assertEquals(StudioTheme.MidnightMetallic, StudioTheme.fromId("midnight"))
  }
}
