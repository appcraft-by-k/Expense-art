package com.appcraftbyk.expense.art.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appcraftbyk.expense.art.R
import com.appcraftbyk.expense.art.data.prefs.ThemeMode
import com.appcraftbyk.expense.art.ui.common.LoadStatus
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.profile_saved)
    val tooLongMessage = stringResource(R.string.profile_name_too_long)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SettingsEffect.ProfileNameSaved -> snackbarHostState.showSnackbar(savedMessage)
                SettingsEffect.ProfileNameTooLong -> snackbarHostState.showSnackbar(tooLongMessage)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when (uiState.status) {
            LoadStatus.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            LoadStatus.Content, LoadStatus.Empty, LoadStatus.Error -> SettingsContent(
                uiState = uiState,
                onThemeModeSelected = viewModel::onThemeModeSelected,
                onProfileNameChange = viewModel::onProfileNameChange,
                onSaveProfileName = viewModel::saveProfileName,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onProfileNameChange: (String) -> Unit,
    onSaveProfileName: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.settings_appearance),
                style = MaterialTheme.typography.titleMedium
            )
            Column(Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { themeMode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected = uiState.themeMode == themeMode,
                                onClick = { onThemeModeSelected(themeMode) },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.themeMode == themeMode,
                            onClick = null
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(themeMode.labelRes()),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.settings_profile),
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = uiState.profileName,
                onValueChange = onProfileNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.profile_name_label)) },
                singleLine = true,
                isError = uiState.isProfileNameTooLong,
                supportingText = {
                    Text(
                        text = if (uiState.isProfileNameTooLong) {
                            stringResource(R.string.profile_name_too_long)
                        } else {
                            stringResource(R.string.profile_name_supporting)
                        }
                    )
                }
            )
            Button(
                onClick = onSaveProfileName,
                enabled = uiState.isProfileNameDirty && !uiState.isProfileNameTooLong
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}
