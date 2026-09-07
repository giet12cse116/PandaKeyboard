package com.panda.keyboards.ui.settings

import com.panda.keyboards.theme.KeyboardHeight
import com.panda.keyboards.theme.KeyboardSettings
import com.panda.keyboards.theme.QwertyOrder
import com.panda.keyboards.theme.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeSettingsRepository(
    initialSettings: KeyboardSettings = KeyboardSettings()
) : SettingsRepository(context = null) {

    private val _settingsFlow = MutableStateFlow(initialSettings)
    override val settings: StateFlow<KeyboardSettings> = _settingsFlow.asStateFlow()

    override suspend fun setQwertyOrder(order: QwertyOrder) {
        _settingsFlow.value = _settingsFlow.value.copy(qwertyOrder = order)
    }

    override suspend fun setKeyboardHeight(height: KeyboardHeight) {
        _settingsFlow.value = _settingsFlow.value.copy(keyboardHeight = height)
    }

    override suspend fun setAutoCorrectionEnabled(enabled: Boolean) {
        _settingsFlow.value = _settingsFlow.value.copy(autoCorrectionEnabled = enabled)
    }

    override suspend fun setOfflineModeEnabled(enabled: Boolean) {
        _settingsFlow.value = _settingsFlow.value.copy(offlineModeEnabled = enabled)
    }

    override suspend fun setNumberRowEnabled(enabled: Boolean) {
        _settingsFlow.value = _settingsFlow.value.copy(numberRowEnabled = enabled)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeSettingsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeSettingsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun settings_exposesDefaultRepositorySettings() = runTest {
        val viewModel = SettingsViewModel(fakeRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.settings.collect {}
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val settings = viewModel.settings.value
        assertEquals(QwertyOrder.QWERTY, settings.qwertyOrder)
        assertEquals(KeyboardHeight.DEFAULT, settings.keyboardHeight)
        assertTrue(settings.autoCorrectionEnabled)
        assertFalse(settings.offlineModeEnabled)
        assertFalse(settings.numberRowEnabled)
    }

    @Test
    fun setQwertyOrder_updatesSettingsState() = runTest {
        val viewModel = SettingsViewModel(fakeRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.settings.collect {}
        }

        viewModel.setQwertyOrder(QwertyOrder.QWERTZ)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(QwertyOrder.QWERTZ, viewModel.settings.value.qwertyOrder)
    }

    @Test
    fun setKeyboardHeight_updatesSettingsState() = runTest {
        val viewModel = SettingsViewModel(fakeRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.settings.collect {}
        }

        viewModel.setKeyboardHeight(KeyboardHeight.TALL)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(KeyboardHeight.TALL, viewModel.settings.value.keyboardHeight)
    }

    @Test
    fun setAutoCorrectionEnabled_updatesSettingsState() = runTest {
        val viewModel = SettingsViewModel(fakeRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.settings.collect {}
        }

        viewModel.setAutoCorrectionEnabled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.settings.value.autoCorrectionEnabled)
    }

    @Test
    fun setOfflineModeEnabled_updatesSettingsState() = runTest {
        val viewModel = SettingsViewModel(fakeRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.settings.collect {}
        }

        viewModel.setOfflineModeEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.settings.value.offlineModeEnabled)
    }

    @Test
    fun setNumberRowEnabled_updatesSettingsState() = runTest {
        val viewModel = SettingsViewModel(fakeRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.settings.collect {}
        }

        viewModel.setNumberRowEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.settings.value.numberRowEnabled)
    }
}

