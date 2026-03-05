package com.blockpuzzle.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.blockpuzzle.model.ColorPalette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(context: Context) {

    private val dataStore = context.dataStore

    val hapticEnabledFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_HAPTIC_ENABLED] ?: false
    }

    val paletteFlow: Flow<ColorPalette> = dataStore.data.map { prefs ->
        val name = prefs[KEY_PALETTE] ?: ColorPalette.JEWEL.name
        try { ColorPalette.valueOf(name) } catch (_: IllegalArgumentException) { ColorPalette.JEWEL }
    }

    suspend fun saveHapticEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun savePalette(palette: ColorPalette) {
        dataStore.edit { prefs ->
            prefs[KEY_PALETTE] = palette.name
        }
    }

    companion object {
        private val KEY_HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        private val KEY_PALETTE = stringPreferencesKey("color_palette")
    }
}
