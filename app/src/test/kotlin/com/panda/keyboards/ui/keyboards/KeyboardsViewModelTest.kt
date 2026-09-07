package com.panda.keyboards.ui.keyboards

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Fake implementation of [ThemeRepository] for isolated ViewModel unit testing.
 */
private class FakeThemeRepository(
    initialThemes: List<KeyboardTheme>,
    initialSelectedId: String? = null
) : ThemeRepository(
    context = null
) {
    private val _allThemes = MutableStateFlow(initialThemes)
    override val themes: StateFlow<List<KeyboardTheme>> = _allThemes

    private val _selectedThemeId = MutableStateFlow<String?>(initialSelectedId)
    override val selectedThemeId: StateFlow<String?> = _selectedThemeId

    var lastSetThemeId: String? = null

    override suspend fun setSelectedThemeId(themeId: String?) {
        lastSetThemeId = themeId
        _selectedThemeId.value = themeId
    }
}

private class FakeContext : android.content.ContextWrapper(null) {
    override fun getPackageName(): String = "com.panda.keyboards"
    override fun getSystemService(name: String): Any? = null
    override fun getContentResolver(): android.content.ContentResolver? = null
}

@OptIn(ExperimentalCoroutinesApi::class)
class KeyboardsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val sampleThemes = (1..10).map { i ->
        KeyboardTheme(
            id = "theme_$i",
            name = "Theme $i",
            isPro = i % 2 == 0,
            keyBackgroundColor = "#3A3A3C",
            keyTextColor = "#FFFFFF",
            keyboardBackground = ThemeBackground.SolidColor("#1C1C1E"),
            keyShape = KeyShape.ROUNDED,
            accentColor = "#7C4DFF"
        )
    }

    private lateinit var fakeRepository: FakeThemeRepository
    private lateinit var fakeImeChecker: ImeStatusChecker

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeThemeRepository(sampleThemes, null)
        fakeImeChecker = ImeStatusChecker(FakeContext())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun selectedThemeId_initiallyNullOnFreshInstall() = runTest {
        val viewModel = KeyboardsViewModel(fakeRepository, fakeImeChecker)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.selectedThemeId.collect {}
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.selectedThemeId.value)
    }

    @Test
    fun themes_initiallyLoadsOnlyFirstPage() = runTest {
        val viewModel = KeyboardsViewModel(fakeRepository, fakeImeChecker)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.themes.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.hasMoreThemes.collect {}
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val initialResult = viewModel.themes.value
        assertEquals(KeyboardsViewModel.INITIAL_PAGE_SIZE, initialResult.size)
        assertEquals("Theme 1", initialResult[0].name)
        assertTrue(viewModel.hasMoreThemes.value)
    }

    @Test
    fun loadMoreThemes_appendsNextBatchOnDemand() = runTest {
        val viewModel = KeyboardsViewModel(fakeRepository, fakeImeChecker)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.themes.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.hasMoreThemes.collect {}
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(6, viewModel.themes.value.size)
        assertTrue(viewModel.hasMoreThemes.value)

        viewModel.loadMoreThemes()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(10, viewModel.themes.value.size)
        assertFalse(viewModel.hasMoreThemes.value)
    }

    @Test
    fun openThemeSheet_and_dismissThemeSheet_manageSheetState() = runTest {
        val viewModel = KeyboardsViewModel(fakeRepository, fakeImeChecker)
        val targetTheme = sampleThemes[0]

        assertNull(viewModel.selectedThemeForSheet.value)

        viewModel.openThemeSheet(targetTheme)
        assertEquals(targetTheme, viewModel.selectedThemeForSheet.value)

        viewModel.dismissThemeSheet()
        assertNull(viewModel.selectedThemeForSheet.value)
    }

    @Test
    fun unlockTheme_addsThemeToUnlockedSet() = runTest {
        val viewModel = KeyboardsViewModel(fakeRepository, fakeImeChecker)

        assertFalse(viewModel.unlockedThemeIds.value.contains("theme_2"))

        viewModel.unlockTheme("theme_2")
        assertTrue(viewModel.unlockedThemeIds.value.contains("theme_2"))
    }

    @Test
    fun applyTheme_updatesRepositorySelectionAndClosesSheet() = runTest {
        val viewModel = KeyboardsViewModel(fakeRepository, fakeImeChecker)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.selectedThemeId.collect {}
        }

        viewModel.openThemeSheet(sampleThemes[1])
        viewModel.applyTheme("theme_2")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("theme_2", fakeRepository.lastSetThemeId)
        assertEquals("theme_2", viewModel.selectedThemeId.value)
        assertNull(viewModel.selectedThemeForSheet.value)
    }

    @Test
    fun watchRewardedAdAndApply_tracksSessionAdsAndGrantsFreeApplyPassesAfterTwoAds() = runTest {
        val viewModel = KeyboardsViewModel(fakeRepository, fakeImeChecker)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.selectedThemeId.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.adsWatchedInSession.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.freeApplyCredits.collect {}
        }

        // 1st ad watch
        viewModel.watchRewardedAdAndApply("theme_1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.adsWatchedInSession.value)
        assertEquals(0, viewModel.freeApplyCredits.value)

        // 2nd ad watch back-to-back
        viewModel.watchRewardedAdAndApply("theme_2")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.adsWatchedInSession.value)
        assertEquals(2, viewModel.freeApplyCredits.value)

        // 1st free apply pass consumption
        viewModel.consumeFreeApplyCreditAndApply("theme_3")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.freeApplyCredits.value)

        // 2nd free apply pass consumption
        viewModel.consumeFreeApplyCreditAndApply("theme_4")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.freeApplyCredits.value)
    }
}

