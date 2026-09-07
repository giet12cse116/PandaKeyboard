package com.panda.keyboards.ui.setup

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
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

private class ThemeReadyFakeThemeRepository(
    initialThemes: List<KeyboardTheme>
) : ThemeRepository(context = null) {
    private val _allThemes = MutableStateFlow(initialThemes)
    override val themes: StateFlow<List<KeyboardTheme>> = _allThemes
}

@OptIn(ExperimentalCoroutinesApi::class)
class ThemeReadyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val sampleThemes = listOf(
        KeyboardTheme(
            id = "theme_pastel",
            name = "Pastel Dream",
            isPro = false,
            keyBackgroundColor = "#F5E6F0",
            keyTextColor = "#4A4A4A",
            keyboardBackground = ThemeBackground.SolidColor("#FFECD2"),
            keyShape = KeyShape.PILL,
            accentColor = "#FF6B9D"
        )
    )

    private lateinit var fakeRepository: ThemeReadyFakeThemeRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = ThemeReadyFakeThemeRepository(sampleThemes)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initAppliedTheme_resolvesAppliedThemeObject() = runTest {
        val viewModel = ThemeReadyViewModel(fakeRepository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.appliedTheme.collect {}
        }

        viewModel.initAppliedTheme("theme_pastel")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("theme_pastel", viewModel.appliedThemeId.value)
        assertNotNull(viewModel.appliedTheme.value)
        assertEquals("Pastel Dream", viewModel.appliedTheme.value?.name)
    }
}
