package com.example.vulnerablehabittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.vulnerablehabittracker.data.Habit
import com.example.vulnerablehabittracker.data.HabitRepository
import com.example.vulnerablehabittracker.ui.OnboardingScreen
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = HabitRepository(this)
        setContent {
            HabitTrackerApp(repository)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerApp(repository: HabitRepository) {
    // Determine initial state based on profile existence
    var showOnboarding by remember { mutableStateOf(repository.loadUserProfile() == null) }
    
    // If we are in onboarding, show that screen
    if (showOnboarding) {
        OnboardingScreen(onSave = { profile ->
            repository.saveUserProfile(profile)
            showOnboarding = false
        })
        return
    }

    // Otherwise, show the main app
    var habits by remember { mutableStateOf(repository.loadHabits()) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vulnerable Habit Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            HabitList(
                habits = habits,
                onUpdate = { updatedHabits ->
                    habits = updatedHabits
                    repository.saveHabits(habits)
                }
            )
        }

        if (showDialog) {
            AddHabitDialog(
                onDismiss = { showDialog = false },
                onAdd = { title, desc, goal ->
                    val newHabit = Habit(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        description = desc,
                        goal = goal.toIntOrNull() ?: 1
                    )
                    // Use a new list to ensure clear state change
                    val updatedHabits = ArrayList(habits)
                    updatedHabits.add(newHabit)
                    habits = updatedHabits
                    repository.saveHabits(habits)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun HabitList(habits: MutableList<Habit>, onUpdate: (MutableList<Habit>) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = habits,
            key = { it.id }
        ) { habit ->
            HabitItem(
                habit = habit,
                onProgressChange = {
                    val index = habits.indexOfFirst { it.id == habit.id }
                    if (index != -1) {
                        // Create a copy of the list
                        val newHabits = ArrayList(habits)
                        // Create a copy of the habit (shallow copy is enough for data class, but we change properties)
                        // Since properties are var, copy() works to create new instance.
                        val newHabit = habit.copy(streak = habit.streak + 1)
                        // Update progress on the new instance
                        newHabit.progress = (newHabit.streak.toFloat() / newHabit.goal.toFloat()).coerceAtMost(1.0f)
                        
                        newHabits[index] = newHabit
                        onUpdate(newHabits)
                    }
                },
                onDelete = {
                    if (habit.title != "no_alcohol") {
                        val newHabits = ArrayList(habits)
                        newHabits.removeIf { it.id == habit.id }
                        onUpdate(newHabits)
                    }
                }
            )
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onProgressChange: () -> Unit, onDelete: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
            
            Text(text = habit.description, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Streak: ${habit.streak} / ${habit.goal}")
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onProgressChange, 
                    modifier = Modifier.height(36.dp),
                    enabled = habit.streak < habit.goal // Disable if goal reached
                ) {
                    Text("+1")
                }
            }
            
            if (habit.streak >= habit.goal) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You achieved your goal!",
                    color = Color(0xFF4CAF50), // Green color
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress Visualization
            LinearProgressIndicator(
                progress = habit.progress,
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
        }
    }
}

@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var goal by remember { mutableStateOf(TextFieldValue("7")) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("New Habit", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = goal,
                    onValueChange = { 
                        // Only allow numeric input
                        if (it.text.all { char -> char.isDigit() }) {
                            goal = it 
                        }
                    },
                    label = { Text("Weekly Goal (Count)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = {
                        if (title.text.isNotEmpty()) {
                            onAdd(title.text, description.text, goal.text)
                        }
                    }) { Text("Add") }
                }
            }
        }
    }
}
