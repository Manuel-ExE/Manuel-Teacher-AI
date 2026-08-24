package com.manuel.tai

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManuelTAiApp(applicationContext)
        }
    }
}

private enum class AppScreen(val label: String, val icon: String) {
    Dashboard("Home", "⌂"),
    LessonPlanner("Lessons", "▣"),
    QuestionGenerator("Questions", "?"),
    Materials("Materials", "▤"),
    Settings("Settings", "⚙")
}

@Composable
private fun ManuelTAiApp(context: Context) {
    val preferences = remember { context.getSharedPreferences("manuel_tai", Context.MODE_PRIVATE) }
    var screen by rememberSaveable { mutableStateOf(AppScreen.Dashboard) }
    var resourceCount by rememberSaveable { mutableIntStateOf(preferences.getInt("resource_count", 0)) }
    var savedLesson by rememberSaveable { mutableStateOf(preferences.getString("saved_lesson", "") ?: "") }
    val importedMaterials = remember {
        mutableStateListOf<String>().apply {
            preferences.getStringSet("materials", emptySet()).orEmpty().forEach(::add)
        }
    }

    fun saveResource() {
        resourceCount += 1
        preferences.edit().putInt("resource_count", resourceCount).apply()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ManuelTAi", fontWeight = FontWeight.Bold)
                        Text("Offline teaching workspace", style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                AppScreen.entries.forEach { item ->
                    NavigationBarItem(
                        selected = screen == item,
                        onClick = { screen = item },
                        icon = { Text(item.icon, style = MaterialTheme.typography.titleMedium) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (screen) {
                AppScreen.Dashboard -> DashboardScreen(
                    resourceCount = resourceCount,
                    onOpen = { screen = it }
                )
                AppScreen.LessonPlanner -> LessonPlannerScreen(
                    savedLesson = savedLesson,
                    onSave = { lesson ->
                        savedLesson = lesson
                        preferences.edit().putString("saved_lesson", lesson).apply()
                        saveResource()
                    }
                )
                AppScreen.QuestionGenerator -> QuestionGeneratorScreen(onSave = ::saveResource)
                AppScreen.Materials -> MaterialsScreen(
                    materials = importedMaterials,
                    onMaterialImported = { name ->
                        if (name !in importedMaterials) importedMaterials.add(name)
                        preferences.edit().putStringSet("materials", importedMaterials.toSet()).apply()
                    }
                )
                AppScreen.Settings -> SettingsScreen(
                    resourceCount = resourceCount,
                    modelTier = preferences.getString("model_tier", "Small") ?: "Small",
                    onModelTierChanged = { tier -> preferences.edit().putString("model_tier", tier).apply() }
                )
            }
        }
    }
}

@Composable
private fun DashboardScreen(resourceCount: Int, onOpen: (AppScreen) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Good morning, Teacher", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Plan lessons, create assessments, and keep your teaching materials on this device.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {}, label = { Text("LOCAL-FIRST") })
                        AssistChip(onClick = {}, label = { Text("BATTERY SAVER") })
                    }
                    Text("AI engine status", style = MaterialTheme.typography.titleMedium)
                    Text("Ready for local model setup. No background AI process is running.")
                }
            }
        }
        item {
            Text("What would you like to do?", style = MaterialTheme.typography.titleLarge)
        }
        item {
            ActionCard(
                title = "Lesson planner",
                description = "Generate a structured lesson from subject, class, topic, and curriculum.",
                action = "Create lesson",
                onClick = { onOpen(AppScreen.LessonPlanner) }
            )
        }
        item {
            ActionCard(
                title = "Question generator",
                description = "Create classroom questions and answer keys for revision or assessment.",
                action = "Create questions",
                onClick = { onOpen(AppScreen.QuestionGenerator) }
            )
        }
        item {
            ActionCard(
                title = "Teaching materials",
                description = "Import curriculum files and keep them available offline.",
                action = "Open library",
                onClick = { onOpen(AppScreen.Materials) }
            )
        }
        item {
            Text("Saved resources: $resourceCount", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ActionCard(title: String, description: String, action: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onClick) { Text(action) }
        }
    }
}

@Composable
private fun LessonPlannerScreen(savedLesson: String, onSave: (String) -> Unit) {
    var subject by rememberSaveable { mutableStateOf("Biology") }
    var classLevel by rememberSaveable { mutableStateOf("JSS 2") }
    var topic by rememberSaveable { mutableStateOf("Photosynthesis") }
    var duration by rememberSaveable { mutableStateOf("40 minutes") }
    var curriculum by rememberSaveable { mutableStateOf("Nigerian Curriculum") }
    var difficulty by rememberSaveable { mutableStateOf("Standard") }
    var generated by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Lesson planner", style = MaterialTheme.typography.headlineSmall)
            Text("Create an editable lesson draft without an internet connection.")
        }
        item { OutlinedTextField(subject, { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(classLevel, { classLevel = it }, label = { Text("Class") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(topic, { topic = it }, label = { Text("Topic") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(duration, { duration = it }, label = { Text("Duration") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(curriculum, { curriculum = it }, label = { Text("Curriculum") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Beginner", "Standard", "Advanced").forEach { value ->
                    FilterChip(selected = difficulty == value, onClick = { difficulty = value }, label = { Text(value) })
                }
            }
        }
        item {
            Button(
                onClick = {
                    generated = lessonDraft(subject, classLevel, topic, duration, curriculum, difficulty)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Generate local lesson draft") }
        }
        if (generated.isNotBlank()) {
            item { DraftCard(title = "Local lesson draft", content = generated, onSave = { onSave(generated) }) }
        }
        if (savedLesson.isNotBlank() && generated.isBlank()) {
            item { DraftCard(title = "Last saved lesson", content = savedLesson, onSave = {}) }
        }
    }
}

@Composable
private fun QuestionGeneratorScreen(onSave: () -> Unit) {
    var subject by rememberSaveable { mutableStateOf("Mathematics") }
    var classLevel by rememberSaveable { mutableStateOf("Primary 6") }
    var topic by rememberSaveable { mutableStateOf("Fractions") }
    var count by rememberSaveable { mutableStateOf("20") }
    var generated by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Question generator", style = MaterialTheme.typography.headlineSmall)
            Text("Generate a local draft with answers for teacher review.")
        }
        item { OutlinedTextField(subject, { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(classLevel, { classLevel = it }, label = { Text("Class") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(topic, { topic = it }, label = { Text("Topic") }, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(count, { count = it }, label = { Text("Number of questions") }, modifier = Modifier.fillMaxWidth()) }
        item {
            Button(
                onClick = { generated = questionDraft(subject, classLevel, topic, count) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Generate local questions") }
        }
        if (generated.isNotBlank()) {
            item { DraftCard(title = "Question set draft", content = generated, onSave = onSave) }
        }
    }
}

@Composable
private fun MaterialsScreen(materials: List<String>, onMaterialImported: (String) -> Unit) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.lastPathSegment?.substringAfterLast('/')?.let(onMaterialImported)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Teaching materials", style = MaterialTheme.typography.headlineSmall)
            Text("Keep curriculum and lesson files available on the device for future local retrieval.")
        }
        item {
            Button(onClick = { picker.launch("application/pdf") }, modifier = Modifier.fillMaxWidth()) {
                Text("Import curriculum PDF")
            }
        }
        item { HorizontalDivider() }
        if (materials.isEmpty()) {
            item { Text("No materials imported yet.", style = MaterialTheme.typography.bodyMedium) }
        } else {
            items(materials) { name ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("▤", modifier = Modifier.size(28.dp))
                        Column {
                            Text(name, fontWeight = FontWeight.Medium)
                            Text("Stored locally", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(resourceCount: Int, modelTier: String, onModelTierChanged: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineSmall)
            Text("ManuelTAi is designed to work quietly and efficiently on Android.")
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Offline AI", style = MaterialTheme.typography.titleLarge)
                    Text("No OpenAI, Claude, or Gemini API is required for the planned local engine.")
                    Text("Current status: interface ready; local model runtime will be connected next.")
                }
            }
        }
        item {
            Text("Model tier", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Small", "Standard").forEach { tier ->
                    FilterChip(selected = modelTier == tier, onClick = { onModelTierChanged(tier) }, label = { Text(tier) })
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Battery saver", style = MaterialTheme.typography.titleMedium)
                    Text("AI generation is user-triggered. The app does not poll in the background, keep a permanent connection, or run continuous inference.")
                }
            }
        }
        item { Text("Saved resources: $resourceCount", style = MaterialTheme.typography.labelLarge) }
    }
}

@Composable
private fun DraftCard(title: String, content: String, onSave: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(content, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSave) { Text("Save") }
                OutlinedButton(onClick = {}) { Text("Edit later") }
            }
        }
    }
}

private fun lessonDraft(
    subject: String,
    classLevel: String,
    topic: String,
    duration: String,
    curriculum: String,
    difficulty: String
): String = """
Topic: $topic
Class: $classLevel · Subject: $subject
Duration: $duration · Level: $difficulty
Curriculum: $curriculum

Learning objectives
• Define and explain the key ideas in $topic.
• Use a worked example to demonstrate understanding.
• Complete a short assessment independently.

Lesson flow
1. Starter: connect $topic to a familiar classroom example.
2. Explanation: introduce the core concept with a simple diagram.
3. Activity: learners work in pairs and explain their reasoning.
4. Assessment: ask five short questions and review responses.
5. Homework: apply the concept to one new example.

Teacher note
This is an offline draft for teacher review. Verify examples and curriculum alignment before classroom use.
""".trimIndent()

private fun questionDraft(subject: String, classLevel: String, topic: String, count: String): String = """
Subject: $subject · Class: $classLevel · Topic: $topic
Requested questions: $count

1. Explain the main idea of $topic in one sentence.
   Suggested answer: A clear definition using the key terms from the lesson.

2. Give one worked example related to $topic.
   Suggested answer: A correct example with the working shown step by step.

3. What mistake should a learner avoid when working with $topic?
   Suggested answer: State one common misconception and the correct method.

Teacher review checklist
• Confirm the questions match the selected curriculum.
• Adjust difficulty for the class.
• Verify every answer before printing or adding to CBT.
""".trimIndent()
