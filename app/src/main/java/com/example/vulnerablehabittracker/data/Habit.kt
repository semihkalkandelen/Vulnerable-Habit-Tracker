package com.example.vulnerablehabittracker.data

data class Habit(
    val id: String,
    val title: String,
    var description: String, // Will be "encrypted"
    val goal: Int, // e.g., times per week
    var streak: Int = 0,
    var progress: Float = 0f // 0.0 to 1.0 for progress bar
)
