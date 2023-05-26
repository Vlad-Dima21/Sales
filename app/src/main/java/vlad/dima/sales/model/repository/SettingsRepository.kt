package vlad.dima.sales.model.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val settingsDataStore: DataStore<Preferences>) {
    suspend fun updateSetting(setting: Preferences.Key<Int>, value: Int) {
        settingsDataStore.edit { settings ->
            settings[setting] = value
        }
    }
    suspend fun updateSetting(setting: Preferences.Key<Boolean>, value: Boolean) {
        settingsDataStore.edit { settings ->
            settings[setting] = value
        }
    }

    fun getSettingValue(setting: Preferences.Key<Int>): Flow<Int?> {
        return settingsDataStore.data.map { settings ->
            settings[setting]
        }
    }

    @JvmName("getSettingValueBoolean")
    fun getSettingValue(setting: Preferences.Key<Boolean>): Flow<Boolean?> {
        return settingsDataStore.data.map { settings ->
            settings[setting]
        }
    }

}