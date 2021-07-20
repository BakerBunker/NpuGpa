package com.bakerbunker.npugpa.util

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore("settings")
val ACCOUNT = stringPreferencesKey("account")
val PASSWORD = stringPreferencesKey("password")
val SELECTED= stringPreferencesKey("selected")