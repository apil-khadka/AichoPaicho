package dev.nyxigale.aichopaicho.viewmodel

import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.work.WorkManager
import androidx.work.impl.WorkManagerImpl
import dev.nyxigale.aichopaicho.AppLocaleManager
import dev.nyxigale.aichopaicho.AppPreferenceUtils
import dev.nyxigale.aichopaicho.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Rule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: SettingsViewModel
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val recordRepository: RecordRepository = mockk(relaxed = true)
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val syncRepository: SyncRepository = mockk(relaxed = true)
    private val syncCenterRepository: SyncCenterRepository = mockk(relaxed = true)
    private val preferencesRepository: PreferencesRepository = mockk(relaxed = true)
    private val csvTransferService: CsvTransferService = mockk(relaxed = true)
    private val activity: Activity = mockk(relaxed = true)
    private val packageManager: PackageManager = mockk(relaxed = true)
    private val packageInfo: PackageInfo = mockk(relaxed = true)

    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockkObject(AppPreferenceUtils)
        mockkObject(AppLocaleManager)

        // Mock WorkManagerImpl.getInstance since WorkManager.getInstance calls it and casts the result.
        val workManagerImpl = mockk<WorkManagerImpl>(relaxed = true)
        mockkStatic(WorkManagerImpl::class)
        every { WorkManagerImpl.getInstance(any()) } returns workManagerImpl

        // We can just mock static WorkManager to return our workManagerImpl which implements WorkManager
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManagerImpl

        mockkStatic(ActivityCompat::class)
        every { ActivityCompat.recreate(any()) } just Runs

        every { context.applicationContext } returns context
        every { context.packageManager } returns packageManager
        every { context.packageName } returns "dev.nyxigale.aichopaicho"
        every { packageManager.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        packageInfo.versionName = "1.0"

        every { context.getString(dev.nyxigale.aichopaicho.R.string.Version_number) } returns "1.0"

        every { AppPreferenceUtils.getLanguageCode(context) } returns "en"
        every { AppPreferenceUtils.getCurrencyCode(context) } returns "NPR"

        viewModel = SettingsViewModel(
            userRepository = userRepository,
            recordRepository = recordRepository,
            contactRepository = contactRepository,
            firebaseAuth = firebaseAuth,
            syncRepository = syncRepository,
            syncCenterRepository = syncCenterRepository,
            preferencesRepository = preferencesRepository,
            csvTransferService = csvTransferService,
            context = context
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `updateLanguage should update preferences, locale, state and recreate activity`() {
        // Arrange
        val newLanguage = "ne" // Nepali language code

        every { AppPreferenceUtils.setLanguageCode(any(), any()) } just Runs
        every { AppLocaleManager.setAppLocale(any(), any()) } returns context

        // Act
        viewModel.updateLanguage(activity, newLanguage)

        // Assert
        verify { AppPreferenceUtils.setLanguageCode(context, newLanguage) }
        verify { AppLocaleManager.setAppLocale(context, newLanguage) }
        verify { ActivityCompat.recreate(activity) }
        assertEquals(newLanguage, viewModel.uiState.value.selectedLanguage)
    }

    @Test
    fun `loadCurrencySettings should update state with current currency`() {
        // Arrange
        val expectedCurrency = "USD"
        every { AppPreferenceUtils.getCurrencyCode(context) } returns expectedCurrency

        // Act
        viewModel.loadCurrencySettings(context)

        // Assert
        verify { AppPreferenceUtils.getCurrencyCode(context) }
        assertEquals(expectedCurrency, viewModel.uiState.value.selectedCurrency)
    }

    @Test
    fun `updateCurrency should update preferences and state`() {
        // Arrange
        val newCurrency = "EUR"
        every { AppPreferenceUtils.setCurrencyCode(any(), any()) } just Runs

        // Act
        viewModel.updateCurrency(context, newCurrency)

        // Assert
        verify { AppPreferenceUtils.setCurrencyCode(context, newCurrency) }
        assertEquals(newCurrency, viewModel.uiState.value.selectedCurrency)
        assertEquals(false, viewModel.uiState.value.showCurrencyDropdown)
    }
}
