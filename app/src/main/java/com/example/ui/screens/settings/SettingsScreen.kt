package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.LyricStudioApp
import com.example.R
import com.example.domain.model.StudioTheme
import com.example.ui.components.StudioButton
import com.example.ui.components.StudioCard
import kotlinx.coroutines.launch

private data class ThemeOption(
    val theme: StudioTheme,
    val titleRes: Int,
    val accent: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToTrash: () -> Unit
) {
    val app = LyricStudioApp.instance
    val prefs = app.preferencesManager
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val currentThemeId by prefs.themeFlow.collectAsState(initial = "midnight")
    val currentLang by prefs.languageFlow.collectAsState(initial = "fa")
    val fontSize by prefs.fontSizeFlow.collectAsState(initial = 16f)
    val lineHeight by prefs.lineHeightFlow.collectAsState(initial = 1.5f)
    val isAppLockEnabled by prefs.appLockEnabledFlow.collectAsState(initial = false)

    var showPinDialog by remember { mutableStateOf(false) }
    var newPinText by remember { mutableStateOf("") }

    val themeList = listOf(
        ThemeOption(StudioTheme.MidnightMetallic, R.string.theme_midnight, Color(0xFF1E88E5)),
        ThemeOption(StudioTheme.PurpleNight, R.string.theme_purple, Color(0xFFBB86FC)),
        ThemeOption(StudioTheme.DeepOcean, R.string.theme_ocean, Color(0xFF00E5FF)),
        ThemeOption(StudioTheme.Emerald, R.string.theme_emerald, Color(0xFF00E676)),
        ThemeOption(StudioTheme.Crimson, R.string.theme_crimson, Color(0xFFFF3355)),
        ThemeOption(StudioTheme.Golden, R.string.theme_golden, Color(0xFFFFD700)),
        ThemeOption(StudioTheme.Neon, R.string.theme_neon, Color(0xFFFF1493)),
        ThemeOption(StudioTheme.Light, R.string.theme_light, Color(0xFF4A68FF))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Theme Selection (§5)
            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.settings_theme),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(themeList, key = { it.theme.id }) { option ->
                            val isSelected = currentThemeId == option.theme.id
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { scope.launch { prefs.setTheme(option.theme.id) } }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(option.accent)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(option.titleRes),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 2. Language Selection (§3)
            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.settings_language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = currentLang == "fa",
                            onClick = { scope.launch { prefs.setLanguage("fa") } },
                            label = { Text("فارسی (Persian)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = currentLang == "en",
                            onClick = { scope.launch { prefs.setLanguage("en") } },
                            label = { Text("English") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Typography & Editor Appearance
            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.settings_typography),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${stringResource(R.string.font_size)}: ${fontSize.toInt()}sp",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = fontSize,
                        onValueChange = { scope.launch { prefs.setFontSize(it) } },
                        valueRange = 12f..28f,
                        steps = 7
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${stringResource(R.string.line_height)}: ${String.format(java.util.Locale.US, "%.1f", lineHeight)}x",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = lineHeight,
                        onValueChange = { scope.launch { prefs.setLineHeight(it) } },
                        valueRange = 1.0f..2.5f,
                        steps = 6
                    )
                }
            }

            // 4. Security & App Lock (§32)
            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_security),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "قفل رمز ورود به برنامه برای حفظ حریم خصوصی ترانه‌ها",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAppLockEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    showPinDialog = true
                                } else {
                                    scope.launch {
                                        prefs.setAppLockEnabled(false)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // 5. Data & Trash Management
            item {
                StudioCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.settings_backup),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    StudioButton(
                        text = stringResource(R.string.trash_title),
                        onClick = onNavigateToTrash,
                        icon = Icons.Default.Delete,
                        isPrimary = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 6. Creator & About Studio Credit
            item {
                StudioCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.splash_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.creator_name),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "نسخه ${stringResource(R.string.app_version)} • ۱۰۰٪ آفلاین و امن",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Set PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(stringResource(R.string.lock_set_pin)) },
            text = {
                OutlinedTextField(
                    value = newPinText,
                    onValueChange = { if (it.length <= 6) newPinText = it },
                    label = { Text("رمز ۴ تا ۶ رقمی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinText.length in 4..6) {
                            scope.launch {
                                prefs.setAppLockPin(newPinText)
                            }
                            showPinDialog = false
                            newPinText = ""
                            Toast.makeText(context, "قفل برنامه فعال شد", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "رمز باید ۴ تا ۶ رقم باشد", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
