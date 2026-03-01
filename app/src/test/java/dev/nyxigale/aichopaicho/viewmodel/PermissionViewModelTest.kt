package dev.nyxigale.aichopaicho.viewmodel

import android.content.Context
import dev.nyxigale.aichopaicho.data.local.ScreenViewRepository
import dev.nyxigale.aichopaicho.viewmodel.data.PermissionScreenUiState
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionViewModelTest {

    private lateinit var viewModel: PermissionViewModel
    private val mockContext: Context = mockk(relaxed = true)
    private val mockRepository: ScreenViewRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PermissionViewModel(
            context = mockContext,
            screenViewRepository = mockRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState should have default values`() {
        // Arrange & Act (handled in setup)
        val currentState = viewModel.uiState.value

        // Assert
        assertEquals(false, currentState.isLoading)
        assertEquals(null, currentState.errorMessage)
        assertEquals(false, currentState.permissionGranted)
    }

    @Test
    fun `setPermissionGranted should update permissionGranted in uiState`() {
        // Act
        viewModel.setPermissionGranted(true)

        // Assert
        assertEquals(true, viewModel.uiState.value.permissionGranted)

        // Act again to verify toggle
        viewModel.setPermissionGranted(false)

        // Assert
        assertEquals(false, viewModel.uiState.value.permissionGranted)
    }
}
