package com.panda.keyboards.ui.setup

import com.panda.keyboards.ime.ImeStatus
import com.panda.keyboards.ime.ImeStatusChecker
import com.panda.keyboards.theme.KeyShape
import com.panda.keyboards.theme.KeyboardTheme
import com.panda.keyboards.theme.ThemeBackground
import com.panda.keyboards.theme.ThemeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeThemeRepository(
    initialThemes: List<KeyboardTheme>
) : ThemeRepository(context = null) {
    private val _allThemes = MutableStateFlow(initialThemes)
    override val themes: StateFlow<List<KeyboardTheme>> = _allThemes

    private val _selectedThemeId = MutableStateFlow<String?>(null)
    override val selectedThemeId: StateFlow<String?> = _selectedThemeId

    var lastAppliedThemeId: String? = null

    override suspend fun setSelectedThemeId(themeId: String?) {
        lastAppliedThemeId = themeId
        _selectedThemeId.value = themeId
    }
}

private class FakeImeStatusChecker : ImeStatusChecker(context = FakeContext()) {
    val statusFlow = MutableStateFlow(ImeStatus(isEnabled = false, isDefault = false))
    override val imeStatus: StateFlow<ImeStatus> = statusFlow
}

private class FakeContext : android.content.ContextWrapper(null) {
    override fun getPackageName(): String = "com.panda.keyboards"
    override fun getSystemService(name: String): Any? = null
    override fun getContentResolver(): android.content.ContentResolver? = null
}

@OptIn(ExperimentalCoroutinesApi::class)
class KeyboardSetupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val sampleThemes = listOf(
        KeyboardTheme(
            id = "theme_midnight",
            name = "Midnight",
            isPro = false,
            keyBackgroundColor = "#1A1A2E",
            keyTextColor = "#E0E0FF",
            keyboardBackground = ThemeBackground.SolidColor("#0F0F23"),
            keyShape = KeyShape.ROUNDED,
            accentColor = "#5C6BC0"
        )
    )

    private lateinit var fakeRepository: FakeThemeRepository
    private lateinit var fakeImeChecker: FakeImeStatusChecker

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeThemeRepository(sampleThemes)
        fakeImeChecker = FakeImeStatusChecker()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initPendingTheme_resolvesPendingThemeObject() = runTest {
        val viewModel = KeyboardSetupViewModel(fakeRepository, fakeImeChecker)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.pendingTheme.collect {}
        }

        viewModel.initPendingTheme("theme_midnight")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("theme_midnight", viewModel.pendingThemeId.value)
        assertNotNull(viewModel.pendingTheme.value)
        assertEquals("Midnight", viewModel.pendingTheme.value?.name)
    }

    @Test
    fun imeStatus_whenFullyConfigured_autoAppliesPendingThemeAndSignalsCompletion() = runTest {
        val viewModel = KeyboardSetupViewModel(fakeRepository, fakeImeChecker)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.setupCompleted.collect {}
        }

        viewModel.initPendingTheme("theme_midnight")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.setupCompleted.value)

        // Simulate user completing both system steps (Enable & Switch)
        fakeImeChecker.statusFlow.value = ImeStatus(isEnabled = true, isDefault = true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("theme_midnight", fakeRepository.lastAppliedThemeId)
        assertTrue(viewModel.setupCompleted.value)
    }
}
