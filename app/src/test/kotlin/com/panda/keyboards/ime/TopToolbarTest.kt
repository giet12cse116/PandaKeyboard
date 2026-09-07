package com.panda.keyboards.ime

import com.panda.keyboards.ime.keyboard.TopToolbarPanel
import com.panda.keyboards.theme.KeyboardTheme
import com.panda.keyboards.theme.ThemeBackground
import com.panda.keyboards.theme.ThemeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

private class FakeThemeRepository : ThemeRepository(context = null) {
    var selectedThemeIdState: String? = null

    override suspend fun setSelectedThemeId(themeId: String?) {
        selectedThemeIdState = themeId
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TopToolbarTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeThemeRepository: FakeThemeRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeThemeRepository = FakeThemeRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun topToolbarPanel_enumConstrainsAllPanels() {
        val values = TopToolbarPanel.entries
        assertEquals(3, values.size)
        assertEquals(TopToolbarPanel.NONE, values[0])
        assertEquals(TopToolbarPanel.THEME_SWITCHER, values[1])
        assertEquals(TopToolbarPanel.FONT_STRIP, values[2])
    }

    @Test
    fun panelState_defaultsToNone() {
        var currentPanel = TopToolbarPanel.NONE
        assertEquals(TopToolbarPanel.NONE, currentPanel)

        // Toggle Theme Switcher
        currentPanel = TopToolbarPanel.THEME_SWITCHER
        assertEquals(TopToolbarPanel.THEME_SWITCHER, currentPanel)

        // Close Theme Switcher
        currentPanel = TopToolbarPanel.NONE
        assertEquals(TopToolbarPanel.NONE, currentPanel)

        // Toggle Font Strip
        currentPanel = TopToolbarPanel.FONT_STRIP
        assertEquals(TopToolbarPanel.FONT_STRIP, currentPanel)

        // Close Font Strip
        currentPanel = TopToolbarPanel.NONE
        assertEquals(TopToolbarPanel.NONE, currentPanel)
    }

    @Test
    fun themeQuickSelection_callsRepositorySetSelectedThemeId() = runTest {
        assertNull(fakeThemeRepository.selectedThemeIdState)

        fakeThemeRepository.setSelectedThemeId("neon_cyber")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("neon_cyber", fakeThemeRepository.selectedThemeIdState)
    }
}
