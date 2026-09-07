package com.panda.keyboards.ui.themeeditor

import com.panda.keyboards.theme.KeyboardTheme
import com.panda.keyboards.theme.ThemeBackground
import com.panda.keyboards.theme.ThemeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeThemeRepository : ThemeRepository(context = null) {
    var savedTheme: KeyboardTheme? = null
    var selectedId: String? = null

    override suspend fun setSelectedThemeId(themeId: String?) {
        selectedId = themeId
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CustomThemeImageTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeThemeRepository
    private lateinit var viewModel: CustomThemeEditorViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeThemeRepository()
        viewModel = CustomThemeEditorViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cropAspectRatio_isFixedAt15() {
        assertEquals("Keyboard aspect ratio must be 1.5:1", 1.5f, CROP_ASPECT_RATIO, 0.001f)
    }

    @Test
    fun viewModel_initialTabIsChooseColors() {
        assertEquals(0, viewModel.selectedTab)
        assertTrue(viewModel.currentDraftTheme.keyboardBackground is ThemeBackground.Gradient)
    }

    @Test
    fun viewModel_uploadImageTabWithCroppedPath_returnsThemeBackgroundImage() {
        viewModel.selectedTab = 1
        viewModel.croppedImagePath = "/data/user/0/com.panda.keyboards/files/custom_theme_images/custom_12345.jpg"
        viewModel.themeName = "My Custom Photo"

        val draft = viewModel.currentDraftTheme
        assertTrue("Draft theme background must be ThemeBackground.Image", draft.keyboardBackground is ThemeBackground.Image)
        val imageBg = draft.keyboardBackground as ThemeBackground.Image
        assertEquals("/data/user/0/com.panda.keyboards/files/custom_theme_images/custom_12345.jpg", imageBg.assetPath)
    }

    @Test
    fun viewModel_cropCancelled_clearsCropSourceBitmap() {
        viewModel.onCropCancelled()
        assertNull(viewModel.imageCropSourceBitmap)
    }

    @Test
    fun viewModel_saveThemeWithImage_persistsImageTheme() = runTest {
        viewModel.selectedTab = 1
        viewModel.croppedImagePath = "/data/user/0/com.panda.keyboards/files/custom_theme_images/custom_9999.jpg"
        viewModel.themeName = "Sunset Keyboard"

        var savedId = ""
        viewModel.saveTheme(context = FakeContextForTest()) { id ->
            savedId = id
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue("Saved theme ID should start with custom_", savedId.startsWith("custom_"))
        assertEquals(savedId, fakeRepository.selectedId)
    }
}

private class FakeContextForTest : android.content.ContextWrapper(null)
