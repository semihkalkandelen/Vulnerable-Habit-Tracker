package com.example.vulnerablehabittracker.data

import android.content.Context
import com.example.vulnerablehabittracker.utils.VulnerabilityUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * IMPLEMENTATION OF VULNERABILITY M6: Inadequate Privacy Controls
 *
 * Weaknesses:
 * 1. Plaintext Storage: Data is stored in a JSON file without encryption (except for the
 *    intentionally weak encryption on the description field).
 * 2. Predictable Location: Stored in the app's files directory.
 * 3. No Integrity Checks: Data can be modified by anyone with root access or through
 *    other vulnerabilities.
 */
class HabitRepository(private val context: Context) {

    private val gson = Gson()
    private val fileName = "habits.json"

    fun saveHabits(habits: List<Habit>) {
        val file = File(context.filesDir, fileName)
        
        // Encrypt descriptions before saving (M10 demonstration)
        // We create a copy to avoid modifying the objects in memory which display on UI
        val habitsToSave = habits.map { it.copy() }
        habitsToSave.forEach { 
            it.description = VulnerabilityUtils.encrypt(it.description)
        }

        try {
            // VULNERABILITY: Writing JSON directly to text file
            // Using writeText defaults to UTF-8
            file.writeText(gson.toJson(habitsToSave), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadHabits(): MutableList<Habit> {
        val file = File(context.filesDir, fileName)
        
        val habits = try {
            if (file.exists()) {
                // Using readText defaults to UTF-8
                val json = file.readText(Charsets.UTF_8)
                val type = object : TypeToken<List<Habit>>() {}.type
                val loadedHabits: MutableList<Habit>? = gson.fromJson(json, type)
                
                // Decrypt descriptions after loading
                loadedHabits?.forEach { 
                    it.description = VulnerabilityUtils.decrypt(it.description)
                }
                
                loadedHabits ?: mutableListOf()
            } else {
                mutableListOf()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }

        // PERMANENT HABIT ENFORCEMENT
        // Ensure "no_alcohol" habit exists
        if (habits.none { it.title == "no_alcohol" }) {
            val permanentHabit = Habit(
                id = "permanent_no_alcohol", // Fixed ID
                title = "no_alcohol",
                description = "why I cannot delete this?",
                goal = 7, // Daily goal implied
                streak = 0
            )
            habits.add(0, permanentHabit) // Add to top
            saveHabits(habits) // Save immediately so it persists
        }

        return habits
    }

    // --- Onboarding / User Profile (M6: Plaintext Storage of PII) ---

    private val profileFileName = "user_profile.json"

    fun saveUserProfile(profile: UserProfile) {
        val file = File(context.filesDir, profileFileName)
        
        // Encrypt PII fields (M10: Insufficient Cryptography)
        // Create copy to not affect UI state if we were using same object (though here we get a fresh one from UI often)
        val profileToSave = profile.copy()
        profileToSave.firstName = VulnerabilityUtils.encrypt(profileToSave.firstName)
        profileToSave.lastName = VulnerabilityUtils.encrypt(profileToSave.lastName)
        profileToSave.email = VulnerabilityUtils.encrypt(profileToSave.email)
        
        try {
            // VULNERABILITY: Writing PII directly to text file without encryption
            // Using writeText defaults to UTF-8
            file.writeText(gson.toJson(profileToSave), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadUserProfile(): UserProfile? {
        val file = File(context.filesDir, profileFileName)
        if (!file.exists()) return null

        return try {
            // Using readText defaults to UTF-8
            val json = file.readText(Charsets.UTF_8)
            val profile: UserProfile? = gson.fromJson(json, UserProfile::class.java)
            
            // Decrypt PII fields
            profile?.apply {
                firstName = VulnerabilityUtils.decrypt(firstName)
                lastName = VulnerabilityUtils.decrypt(lastName)
                email = VulnerabilityUtils.decrypt(email)
            }
            
            profile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
