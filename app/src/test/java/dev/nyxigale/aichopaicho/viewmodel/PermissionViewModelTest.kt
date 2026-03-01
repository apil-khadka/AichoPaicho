package dev.nyxigale.aichopaicho.viewmodel

import android.content.Context
import dev.nyxigale.aichopaicho.R
import dev.nyxigale.aichopaicho.data.local.ScreenViewRepository
import dev.nyxigale.aichopaicho.ui.navigation.Routes
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var screenViewRepository: ScreenViewRepository
    private lateinit var viewModel: PermissionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = mockk()
        screenViewRepository = mockk()

        // Mocking Log calls to avoid exceptions since android.util.Log is not mocked
        io.mockk.mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any(), any()) } returns 0

        viewModel = PermissionViewModel(context, screenViewRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun grantPermissionAndProceed_error_setsErrorMessage() = runTest {
        // Arrange
        val expectedErrorMessage = "Failed to save permission status"
        every { context.getString(R.string.failed_to_save_permission_status) } returns expectedErrorMessage

        val exception = RuntimeException("Database error")
        coEvery { screenViewRepository.markScreenAsShown(Routes.PERMISSION_CONTACTS_SCREEN) } throws exception

        // Act
        val result = viewModel.grantPermissionAndProceed()

        // Assert
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())

        val state = viewModel.uiState.value
        assertEquals(expectedErrorMessage, state.errorMessage)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun grantPermissionAndProceed_success_clearsError() = runTest {
        // Arrange
        coEvery { screenViewRepository.markScreenAsShown(Routes.PERMISSION_CONTACTS_SCREEN) } returns Unit

        // Act
        val result = viewModel.grantPermissionAndProceed()

        // Assert
        assertTrue(result.isSuccess)

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertEquals(false, state.isLoading)
    }
}
