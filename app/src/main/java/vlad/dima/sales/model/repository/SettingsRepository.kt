package vlad.dima.sales.model.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vlad.dima.sales.R

class SettingsRepository(private val settingsDataStore: DataStore<Preferences>) {

    private val isHintHiddenKey = booleanPreferencesKey("isPastSalesHintHidden")
    private val preferredImportance = stringPreferencesKey("salesmanPreferredImportance")

    fun isHintHidden(): Flow<Boolean?> {
        return getSettingValue(isHintHiddenKey)
    }

    suspend fun toggleHintHidden() {
        updateSetting(isHintHiddenKey, true)
    }

    fun salesmanPreferredImportance(): Flow<NotificationImportance> {
        return getSettingValue(preferredImportance).map {
            when(it) {
                "high" -> NotificationImportance.High
                "alert" -> NotificationImportance.Alert
                else -> NotificationImportance.Normal
            }
        }
    }

    suspend fun setSalesmanPreferredImportance(importance: NotificationImportance) {
        updateSetting(
            setting = preferredImportance,
            value = when(importance) {
                NotificationImportance.High -> "high"
                NotificationImportance.Alert -> "alert"
                else -> "normal"
            }
        )
    }

    enum class NotificationImportance(val stringId: Int, val number: Int) {
        Normal(R.string.NormalImportance, 0),
        High(R.string.HightImportance, 1),
        Alert(R.string.AlertImportance, 2)
    }

    private suspend fun updateSetting(setting: Preferences.Key<String>, value: String) {
        settingsDataStore.edit { settings ->
            settings[setting] = value
        }
    }
    private suspend fun updateSetting(setting: Preferences.Key<Boolean>, value: Boolean) {
        settingsDataStore.edit { settings ->
            settings[setting] = value
        }
    }

    fun getSettingValue(setting: Preferences.Key<String>): Flow<String?> {
        return settingsDataStore.data.map { settings ->
            settings[setting]
        }
    }

    @JvmName("getSettingValueBoolean")
    private fun getSettingValue(setting: Preferences.Key<Boolean>): Flow<Boolean?> {
        return settingsDataStore.data.map { settings ->
            settings[setting]
        }
    }



}