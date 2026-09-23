package com.example.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.StudioCard
import com.example.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onNavigateToRoute: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tools_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            // Category 1: Music & Rhythm
            item {
                ToolCategorySection(
                    title = stringResource(R.string.cat_music),
                    tools = listOf(
                        ToolItemData(
                            title = stringResource(R.string.tool_metronome),
                            desc = "مترونوم ضرب‌آهنگ، تنظیم تمپو و تپ تمپو لمسی",
                            icon = Icons.Default.Timer,
                            route = Screen.Metronome.route
                        ),
                        ToolItemData(
                            title = stringResource(R.string.tool_templates),
                            desc = "قالب‌های آماده برای سبک‌های رپ، پاپ، آر اند بی و شعر",
                            icon = Icons.Default.DashboardCustomize,
                            route = Screen.Templates.route
                        )
                    ),
                    onToolClick = onNavigateToRoute
                )
            }

            // Category 2: Writing & Rhyme
            item {
                ToolCategorySection(
                    title = stringResource(R.string.cat_writing),
                    tools = listOf(
                        ToolItemData(
                            title = stringResource(R.string.tool_rhyme),
                            desc = "قافیه‌یاب آفلاین فارسی و انگلیسی بدون نیاز به اینترنت",
                            icon = Icons.Default.Spellcheck,
                            route = Screen.RhymeFinder.route
                        ),
                        ToolItemData(
                            title = stringResource(R.string.tool_punchlines),
                            desc = "بیت‌های طلایی، پانچ‌لاین‌ها و جملات سنگین با کپی سریع",
                            icon = Icons.Default.Bolt,
                            route = Screen.Punchlines.route
                        ),
                        ToolItemData(
                            title = stringResource(R.string.tool_wordbank),
                            desc = "گنجینه اصطلاحات و واژگان خاص برای ترانه‌سرایی",
                            icon = Icons.Default.MenuBook,
                            route = Screen.WordBank.route
                        )
                    ),
                    onToolClick = onNavigateToRoute
                )
            }

            // Category 3: Audio & Voice
            item {
                ToolCategorySection(
                    title = stringResource(R.string.cat_audio),
                    tools = listOf(
                        ToolItemData(
                            title = stringResource(R.string.tool_voice),
                            desc = "ضبط یادداشت‌های صوتی، زمزمه ملودی‌ها و ایده‌های صوتی",
                            icon = Icons.Default.Mic,
                            route = Screen.VoiceMemos.route
                        )
                    ),
                    onToolClick = onNavigateToRoute
                )
            }

            // Category 4: Goals & Streak
            item {
                ToolCategorySection(
                    title = stringResource(R.string.cat_ideas),
                    tools = listOf(
                        ToolItemData(
                            title = stringResource(R.string.tool_goals),
                            desc = "تعیین اهداف روزانه کلمات و ابیات برای پیوستگی نگارش",
                            icon = Icons.Default.TrackChanges,
                            route = Screen.Goals.route
                        ),
                        ToolItemData(
                            title = stringResource(R.string.tool_streak),
                            desc = "استریک پیوسته ترانه‌سرایی و تقویم فعالیت استودیو",
                            icon = Icons.Default.CalendarMonth,
                            route = Screen.StreakCalendar.route
                        )
                    ),
                    onToolClick = onNavigateToRoute
                )
            }
        }
    }
}

data class ToolItemData(
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val route: String
)

@Composable
private fun ToolCategorySection(
    title: String,
    tools: List<ToolItemData>,
    onToolClick: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            tools.forEach { tool ->
                StudioCard(
                    onClick = { onToolClick(tool.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tool.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tool.desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
