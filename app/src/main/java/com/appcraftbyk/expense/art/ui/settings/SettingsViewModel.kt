package com.appcraftbyk.expense.art.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcraftbyk.expense.art.data.prefs.PreferencesRepository
import com.appcraftbyk.expense.art.data.prefs.ThemeMode
import com.appcraftbyk.expense.art.ui.common.LoadStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val status: LoadStatus = LoadStatus.Loading,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val profileName: String = "",
    val isProfileNameDirty: Boolean = false,
    val isProfileNameTooLong: Boolean = false
)

sealed interface SettingsEffect {
    data object ProfileNameSaved : SettingsEffect
    data object ProfileNameTooLong : SettingsEffect
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    /** `null` means "not edited yet", so the field follows the persisted value. */
    private val profileNameDraft = MutableStateFlow<String?>(null)
    private val _effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.userPreferences,
        profileNameDraft
    ) { prefs, draft ->
        val savedName = prefs.profileName.orEmpty()
        val current = draft ?: savedName
        SettingsUiState(
            status = LoadStatus.Content,
            themeMode = prefs.themeMode,
            profileName = current,
            isProfileNameDirty = draft != null && current.trim() != savedName,
            isProfileNameTooLong = current.trim().length > MAX_PROFILE_NAME_LENGTH
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsUiState()
    )

    fun onThemeModeSelected(themeMode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(themeMode) }
    }

    fun onProfileNameChange(value: String) {
        profileNameDraft.value = value
    }

    fun saveProfileName() {
        val draft = profileNameDraft.value ?: return
        val trimmed = draft.trim()
        if (trimmed.length > MAX_PROFILE_NAME_LENGTH) {
            viewModelScope.launch { _effects.send(SettingsEffect.ProfileNameTooLong) }
            return
        }
        viewModelScope.launch {
            preferencesRepository.setProfileName(trimmed.takeIf { it.isNotEmpty() })
            // Keep the field on the saved value; the prefs flow will confirm it without a flicker.
            profileNameDraft.value = trimmed
            _effects.send(SettingsEffect.ProfileNameSaved)
        }
    }

    companion object {
        const val MAX_PROFILE_NAME_LENGTH = 40
    }
}
