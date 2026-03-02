package com.blockpuzzle.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "block_puzzle_prefs")

class HighScoreRepository(context: Context) {

    private val dataStore = context.dataStore

    val highScoreFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_HIGH_SCORE] ?: 0
    }

    suspend fun saveHighScore(score: Int) {
        dataStore.edit { prefs ->
            prefs[KEY_HIGH_SCORE] = score
        }
    }

    companion object {
        private val KEY_HIGH_SCORE = intPreferencesKey("high_score")
    }
}
