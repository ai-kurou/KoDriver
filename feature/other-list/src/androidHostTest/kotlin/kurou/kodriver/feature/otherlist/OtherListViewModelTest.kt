@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherlist

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.HapticFeedbackAvailabilityRepository
import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.StartupEnabledRepository
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.CheckHapticFeedbackAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.SaveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.SaveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.StartupRegistrationUseCases
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Android実機（SDK 31+）における buildOtherListItems() とのHapticFeedback項目のフィルタリング連携を確認する。
 * :core:data のプラットフォーム振り分けにより、jvmTestではHapticFeedback項目自体が定義されないため
 * ここでのみ検証する（他のケースは [OtherListViewModelTest]（jvmTest）を参照）。
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OtherListViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var appUpdateRepository: AppUpdateRepository

    @MockK
    private lateinit var keepScreenOnRepository: KeepScreenOnEnabledRepository

    @MockK
    private lateinit var dynamicColorRepository: DynamicColorEnabledRepository

    @MockK
    private lateinit var hapticFeedbackEnabledRepository: HapticFeedbackEnabledRepository

    @MockK
    private lateinit var hapticFeedbackAvailabilityRepository: HapticFeedbackAvailabilityRepository

    @MockK
    private lateinit var startupRegistrationRepository: StartupEnabledRepository

    private val keepScreenOnFlow = MutableStateFlow(true)
    private val dynamicColorFlow = MutableStateFlow(false)
    private val hapticFeedbackFlow = MutableStateFlow(true)

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(hapticFeedbackAvailable: Boolean): OtherListViewModel {
        every { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() } returns hapticFeedbackAvailable
        return OtherListViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(appUpdateRepository),
            observeKeepScreenOn = ObserveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
            saveKeepScreenOn = SaveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
            observeDynamicColorEnabled = ObserveDynamicColorEnabledUseCase(dynamicColorRepository),
            saveDynamicColorEnabled = SaveDynamicColorEnabledUseCase(dynamicColorRepository),
            observeHapticFeedbackEnabled = ObserveHapticFeedbackEnabledUseCase(hapticFeedbackEnabledRepository),
            saveHapticFeedbackEnabled = SaveHapticFeedbackEnabledUseCase(hapticFeedbackEnabledRepository),
            checkHapticFeedbackAvailable = CheckHapticFeedbackAvailableUseCase(hapticFeedbackAvailabilityRepository),
            startupRegistration = StartupRegistrationUseCases(startupRegistrationRepository),
            appVersionInfo =
                OtherListAppVersionInfo(
                    currentVersion = "0.5.0",
                    appVersionLabel = "Androidアプリバージョン",
                ),
        )
    }

    @Test
    fun `振動機能が利用可能な端末ではハプティックフィードバック項目が表示される`() =
        runTest {
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
            every { dynamicColorRepository.dynamicColorEnabled() } returns dynamicColorFlow
            every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns hapticFeedbackFlow
            val viewModel = createViewModel(hapticFeedbackAvailable = true)

            assertTrue(
                viewModel.uiState
                    .first()
                    .items
                    .contains(OtherListItemType.HapticFeedback),
            )
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() }
            confirmVerified(
                appUpdateRepository,
                keepScreenOnRepository,
                dynamicColorRepository,
                hapticFeedbackEnabledRepository,
                hapticFeedbackAvailabilityRepository,
            )
        }

    @Test
    fun `振動機能が利用不可な端末ではハプティックフィードバック項目が表示されない`() =
        runTest {
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
            every { dynamicColorRepository.dynamicColorEnabled() } returns dynamicColorFlow
            every { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() } returns hapticFeedbackFlow
            val viewModel = createViewModel(hapticFeedbackAvailable = false)

            assertFalse(
                viewModel.uiState
                    .first()
                    .items
                    .contains(OtherListItemType.HapticFeedback),
            )
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackEnabledRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { hapticFeedbackAvailabilityRepository.isHapticFeedbackAvailable() }
            confirmVerified(
                appUpdateRepository,
                keepScreenOnRepository,
                dynamicColorRepository,
                hapticFeedbackEnabledRepository,
                hapticFeedbackAvailabilityRepository,
            )
        }
}
