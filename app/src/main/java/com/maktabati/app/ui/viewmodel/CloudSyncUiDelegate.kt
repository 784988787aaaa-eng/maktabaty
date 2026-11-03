package com.maktabati.app.ui.viewmodel

import android.app.Activity
import android.content.Intent
import com.maktabati.app.data.backup.CloudBackupManager
import com.maktabati.app.data.backup.CloudSyncState
import com.maktabati.app.data.backup.SignInOutcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CloudSyncUiDelegate(
    private val scope: CoroutineScope,
    private val cloudBackupManager: CloudBackupManager,
    private val onPreferencesRestored: () -> Unit
) {
    private val _connectedGoogleEmail = MutableStateFlow(cloudBackupManager.getConnectedEmail())
    val connectedGoogleEmail: StateFlow<String?> = _connectedGoogleEmail.asStateFlow()

    private val _lastBackupTimeText = MutableStateFlow(cloudBackupManager.getLastBackupTimeText())
    val lastBackupTimeText: StateFlow<String?> = _lastBackupTimeText.asStateFlow()

    private val _cloudSyncState = MutableStateFlow<CloudSyncState>(CloudSyncState.Idle)
    val cloudSyncState: StateFlow<CloudSyncState> = _cloudSyncState.asStateFlow()

    private var autoBackupJob: Job? = null

    fun refreshAccountInfo() {
        _connectedGoogleEmail.value = cloudBackupManager.getConnectedEmail()
        _lastBackupTimeText.value = cloudBackupManager.getLastBackupTimeText()
    }

    fun prepareGoogleSignInIntent(): Intent? {
        if (!cloudBackupManager.isInternetAvailable()) {
            _cloudSyncState.value = CloudSyncState.Error(CloudBackupManager.MSG_NO_INTERNET)
            return null
        }
        return cloudBackupManager.buildSignInIntent()
    }

    fun handleGoogleSignInResult(data: Intent?, resultCode: Int) {
        when (val outcome = cloudBackupManager.handleSignInResult(data)) {
            is SignInOutcome.Success -> {
                _connectedGoogleEmail.value = outcome.email
                _cloudSyncState.value = CloudSyncState.Success("تم الاتصال بحساب Google بنجاح ☁️")
            }
            is SignInOutcome.Cancelled -> Unit
            is SignInOutcome.Error -> {
                if (resultCode == Activity.RESULT_CANCELED && data == null) {
                    return
                }
                _cloudSyncState.value = CloudSyncState.Error(outcome.arabicMessage)
            }
        }
    }

    fun signOutGoogleCloud() {
        scope.launch {
            cloudBackupManager.signOut()
            _connectedGoogleEmail.value = null
            _cloudSyncState.value = CloudSyncState.Idle
        }
    }

    fun backupLibraryToCloudNow() {
        if (_connectedGoogleEmail.value == null) return
        if (!cloudBackupManager.isInternetAvailable()) {
            _cloudSyncState.value = CloudSyncState.Error(CloudBackupManager.MSG_NO_INTERNET)
            return
        }
        scope.launch {
            _cloudSyncState.value = CloudSyncState.Syncing("جاري حفظ النسخة الاحتياطية في السحابة...")
            val result = cloudBackupManager.createAndUploadBackup()
            result.onSuccess { timestampText ->
                _lastBackupTimeText.value = timestampText
                _cloudSyncState.value = CloudSyncState.Success("تم حفظ النسخة الاحتياطية في السحابة بنجاح ✨")
            }.onFailure { err ->
                _cloudSyncState.value = CloudSyncState.Error(
                    err.message ?: CloudBackupManager.MSG_GENERIC_CLOUD_ERROR
                )
            }
        }
    }

    fun restoreLibraryFromCloudNow() {
        if (_connectedGoogleEmail.value == null) return
        if (!cloudBackupManager.isInternetAvailable()) {
            _cloudSyncState.value = CloudSyncState.Error(CloudBackupManager.MSG_NO_INTERNET)
            return
        }
        scope.launch {
            _cloudSyncState.value = CloudSyncState.Syncing("جاري استرداد النسخة الاحتياطية من السحابة...")
            val result = cloudBackupManager.downloadAndRestoreBackup()
            result.onSuccess { restoredCount ->
                onPreferencesRestored()
                _lastBackupTimeText.value = cloudBackupManager.getLastBackupTimeText()
                _cloudSyncState.value = CloudSyncState.Success(
                    "تم استرداد النسخة الاحتياطية ($restoredCount كتاب) بنجاح ✨"
                )
            }.onFailure { err ->
                _cloudSyncState.value = CloudSyncState.Error(
                    err.message ?: CloudBackupManager.MSG_GENERIC_CLOUD_ERROR
                )
            }
        }
    }

    fun triggerAutoCloudBackup() {
        if (_connectedGoogleEmail.value == null) return
        if (!cloudBackupManager.isInternetAvailable()) return
        autoBackupJob?.cancel()
        autoBackupJob = scope.launch {
            val result = cloudBackupManager.createAndUploadBackup()
            result.onSuccess { timestampText ->
                _lastBackupTimeText.value = timestampText
            }
        }
    }

    fun dismissCloudSyncState() {
        _cloudSyncState.value = CloudSyncState.Idle
    }
}
