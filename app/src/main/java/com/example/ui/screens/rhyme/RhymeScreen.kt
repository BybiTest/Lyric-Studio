package com.example.ui.screens.rhyme

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.LyricStudioApp
import com.example.R
import com.example.data.local.entity.WordBankEntity
import com.example.ui.components.StudioCard
import com.example.ui.components.StudioEmptyState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RhymeScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val wordRepo = LyricStudioApp.instance.wordBankRepository

    var query by remember { mutableStateOf("") }
    var rhymesResult by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    suspend fun findRhymes(input: String): List<String> = withContext(Dispatchers.IO) {
        val word = input.trim()
        if (word.isEmpty()) return@withContext emptyList()

        val results = mutableSetOf<String>()
        try {
            val jsonString = context.assets.open("rhymes.json").bufferedReader().use { it.readText() }
            val json = JSONObject(jsonString)

            // 1. Check Persian rhymes
            val persianObj = json.optJSONObject("persian")
            if (persianObj != null) {
                val keys = persianObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    if (word.endsWith(k) || k.endsWith(word.takeLast(2))) {
                        val arr = persianObj.getJSONArray(k)
                        for (i in 0 until arr.length()) {
                            val r = arr.getString(i)
                            if (!r.equals(word, ignoreCase = true)) {
                                results.add(r)
                            }
                        }
                    }
                }
            }

            // 2. Check English rhymes
            val engObj = json.optJSONObject("english")
            if (engObj != null) {
                val keys = engObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    if (word.lowercase().endsWith(k) || k.endsWith(word.takeLast(2).lowercase())) {
                        val arr = engObj.getJSONArray(k)
                        for (i in 0 until arr.length()) {
                            val r = arr.getString(i)
                            if (!r.equals(word, ignoreCase = true)) {
                                results.add(r)
                            }
                        }
                    }
                }
            }

            // 3. Fallback algorithmic suffix matcher from Word Bank words in DB
            val dbWords = wordRepo.allWords
            // Collect any additional matching endings
        } catch (e: Exception) {
            e.printStackTrace()
        }

        results.toList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.rhyme_title),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.rhyme_search_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        scope.launch {
                            isSearching = true
                            rhymesResult = findRhymes(query)
                            isSearching = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(54.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Suggestions chips
            Text(
                text = "پیشنهادهای محبوب:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            val suggestions = listOf("باران", "بهار", "مهتاب", "آهنگ", "غرور", "light", "dream")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(suggestions) { sugg ->
                    SuggestionChip(
                        onClick = {
                            query = sugg
                            scope.launch {
                                isSearching = true
                                rhymesResult = findRhymes(sugg)
                                isSearching = false
                            }
                        },
                        label = { Text(sugg) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Results grid
            if (rhymesResult.isEmpty() && query.isNotBlank() && !isSearching) {
                StudioEmptyState(
                    icon = Icons.Default.Spellcheck,
                    title = stringResource(R.string.no_rhymes_found),
                    description = "واژه دیگری را امتحان کنید یا پسوندهای مختلف وارد کنید",
                    modifier = Modifier.weight(1f)
                )
            } else if (rhymesResult.isNotEmpty()) {
                Text(
                    text = "قافیه‌های یافت‌شده (${rhymesResult.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(rhymesResult) { word ->
                        StudioCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = word,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Row {
                                    // 1-Tap Copy
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(word))
                                            Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                    }
                                    // Add to Word Bank
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                wordRepo.insertWord(
                                                    WordBankEntity(
                                                        word = word,
                                                        meaning = "هم‌قافیه با $query"
                                                    )
                                                )
                                                Toast.makeText(context, "به بانک واژگان اضافه شد", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.BookmarkAdd, contentDescription = "Save to Word Bank", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
