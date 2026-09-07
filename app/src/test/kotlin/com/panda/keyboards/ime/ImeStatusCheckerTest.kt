package com.panda.keyboards.ime

import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeContext : ContextWrapper(null) {
    override fun getPackageName(): String = "com.panda.keyboards"
    override fun getSystemService(name: String): Any? = null
    override fun getContentResolver(): ContentResolver? = null
}

@OptIn(ExperimentalCoroutinesApi::class)
class ImeStatusCheckerTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial status defaults to unconfigured`() {
        val status = ImeStatus()
        assertFalse(status.isEnabled)
        assertFalse(status.isDefault)
        assertFalse(status.isFullyConfigured)
    }

    @Test
    fun `isFullyConfigured requires both enabled and default to be true`() {
        assertFalse(ImeStatus(isEnabled = false, isDefault = false).isFullyConfigured)
        assertFalse(ImeStatus(isEnabled = true, isDefault = false).isFullyConfigured)
        assertFalse(ImeStatus(isEnabled = false, isDefault = true).isFullyConfigured)
        assertTrue(ImeStatus(isEnabled = true, isDefault = true).isFullyConfigured)
    }

    @Test
    fun `checker initialized with fake context returns safe default status`() = runTest {
        val checker = ImeStatusChecker(context = FakeContext())
        val currentStatus = checker.imeStatus.value
        assertFalse(currentStatus.isEnabled)
        assertFalse(currentStatus.isDefault)
        assertFalse(currentStatus.isFullyConfigured)
    }

    @Test
    fun `refresh triggers status emission safely`() = runTest {
        val checker = ImeStatusChecker(context = FakeContext())
        checker.refresh()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(ImeStatus(isEnabled = false, isDefault = false), checker.imeStatus.value)
    }
}
