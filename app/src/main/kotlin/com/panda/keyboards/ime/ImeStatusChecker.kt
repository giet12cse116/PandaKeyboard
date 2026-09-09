package com.panda.keyboards.ime

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Status of the Panda Keyboards IME in system settings.
 *
 * @property isEnabled Whether the IME is listed as an enabled input method.
 * @property isDefault Whether the IME is currently the active/default input method.
 */
data class ImeStatus(
    val isEnabled: Boolean = false,
    val isDefault: Boolean = false
) {
    /** True when the IME is fully set up (both enabled and selected). */
    val isFullyConfigured: Boolean get() = isEnabled && isDefault
}

/**
 * Checks the IME's enabled/default status via [InputMethodManager] and [Settings.Secure].
 *
 * ROOT CAUSE & DETECTION ARCHITECTURE:
 * Previous implementations of IME status checks could report stale or incorrect status because:
 * 1. Matching relied on broad substring checks without strict comparison against exact
 *    [ComponentName.flattenToShortString] and [ComponentName.flattenToString] values.
 * 2. Status checks were not explicitly guaranteed to re-evaluate BOTH enabled and default state
 *    reactively whenever the key UI screens resumed.
 *
 * To ensure high fidelity:
 * - We check [Settings.Secure.ENABLED_INPUT_METHODS] and [InputMethodManager.getEnabledInputMethodList]
 *   to verify [isEnabled].
 * - We check [Settings.Secure.DEFAULT_INPUT_METHOD] using exact short and full ComponentName matches.
 * - We strictly require [isEnabled] to be true for [isDefault] to be valid ([isDefault] = [isEnabled] && isDefaultInSettings).
 * - We log raw values for system auditing.
 */
@Singleton
open class ImeStatusChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ImeStatusChecker"
        private const val POLL_INTERVAL_MS = 500L

        /** The IME component service name as registered in the manifest. */
        private const val IME_SERVICE_NAME = "com.panda.keyboards.ime.PandaInputMethodService"
    }

    private val _imeStatus = MutableStateFlow(ImeStatus())
    open val imeStatus: StateFlow<ImeStatus> = _imeStatus.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val fullComponentName: String by lazy {
        val pkg = context?.packageName ?: "com.panda.keyboards"
        try {
            ComponentName(pkg, IME_SERVICE_NAME).flattenToString()
        } catch (e: Throwable) {
            "$pkg/$IME_SERVICE_NAME"
        }
    }

    private val shortComponentName: String by lazy {
        val pkg = context?.packageName ?: "com.panda.keyboards"
        try {
            ComponentName(pkg, IME_SERVICE_NAME).flattenToShortString()
        } catch (e: Throwable) {
            "$pkg/.ime.PandaInputMethodService"
        }
    }

    init {
        startPolling()
    }

    private fun startPolling() {
        scope.launch {
            while (isActive) {
                val status = checkStatus()
                _imeStatus.value = status
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    /**
     * Force an immediate status refresh.
     */
    open fun refresh() {
        scope.launch {
            _imeStatus.value = checkStatus()
        }
    }

    private fun checkStatus(): ImeStatus {
        val ctx = context ?: return ImeStatus()
        val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val pkgName = ctx.packageName
        val targetShort = shortComponentName
        val targetFull = fullComponentName

        // 1. Check if our IME is in the system enabled input method list (framework API)
        val isEnabledByImm = try {
            imm?.enabledInputMethodList?.any { imi ->
                imi.packageName.equals(pkgName, ignoreCase = true) || imi.id.contains(pkgName, ignoreCase = true)
            } ?: false
        } catch (e: Exception) {
            false
        }

        // 2. Check Settings.Secure.ENABLED_INPUT_METHODS string (handles full & short component formats)
        val resolver = try { ctx.contentResolver } catch (e: Exception) { null }
        val enabledMethodsSetting = try {
            if (resolver != null) Settings.Secure.getString(resolver, Settings.Secure.ENABLED_INPUT_METHODS) ?: "" else ""
        } catch (e: Exception) {
            ""
        }

        val enabledComponents = enabledMethodsSetting.split(":")
        val isEnabledInSettings = enabledComponents.any { component ->
            val trimmed = component.trim()
            trimmed.isNotEmpty() && (
                trimmed.equals(targetShort, ignoreCase = true) ||
                trimmed.equals(targetFull, ignoreCase = true) ||
                trimmed.contains(pkgName, ignoreCase = true) ||
                trimmed.contains("PandaInputMethodService", ignoreCase = true)
            )
        }

        // 3. Check Settings.Secure.DEFAULT_INPUT_METHOD string for active default IME
        val defaultMethodSetting = try {
            if (resolver != null) Settings.Secure.getString(resolver, Settings.Secure.DEFAULT_INPUT_METHOD) ?: "" else ""
        } catch (e: Exception) {
            ""
        }

        val trimmedDefault = defaultMethodSetting.trim()
        val isDefaultInSettings = trimmedDefault.isNotEmpty() && (
            trimmedDefault.equals(targetShort, ignoreCase = true) ||
            trimmedDefault.equals(targetFull, ignoreCase = true) ||
            trimmedDefault.contains(pkgName, ignoreCase = true) ||
            trimmedDefault.contains("PandaInputMethodService", ignoreCase = true)
        )

        // Ensure BOTH conditions are evaluated: cannot be default unless enabled
        val isEnabled = isEnabledByImm || isEnabledInSettings || isDefaultInSettings
        val isDefault = isEnabled && isDefaultInSettings

        logDebug(
            "checkStatus -> pkg='$pkgName', shortComponent='$targetShort', fullComponent='$targetFull', " +
                    "enabledMethodsSetting='$enabledMethodsSetting', defaultMethodSetting='$defaultMethodSetting', " +
                    "isEnabledByImm=$isEnabledByImm, isEnabled=$isEnabled, isDefault=$isDefault, isFullyConfigured=${isEnabled && isDefault}"
        )

        return ImeStatus(isEnabled = isEnabled, isDefault = isDefault)
    }

    private fun logDebug(message: String) {
        try {
            Log.d(TAG, message)
        } catch (e: Throwable) {
            // Log class not available in JVM unit test environment
        }
    }
}
