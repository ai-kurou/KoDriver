package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.THEME_MODE_DEFAULT
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.HapticFeedbackEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kurou.kodriver.domain.usecase.ObserveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveThemeModeUseCase
import kurou.kodriver.domain.usecase.SaveDynamicColorEnabledUseCase
import kurou.kodriver.domain.usecase.SaveHapticFeedbackEnabledUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnEnabledUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherListDeviceSettingsFlowTest {
    private val keepScreenOnRepository: KeepScreenOnEnabledRepository = mockk()
    private val dynamicColorRepository: DynamicColorEnabledRepository = mockk()
    private val hapticFeedbackRepository: HapticFeedbackEnabledRepository = mockk()
    private val themeRepository: ThemePreferencesRepository = mockk()

    @Test
    fun `初期状態と全設定の更新を状態に反映する`() =
        runTest {
            val keepScreenOn = MutableStateFlow(false)
            val dynamicColor = MutableStateFlow(false)
            val hapticFeedback = MutableStateFlow(false)
            val themeMode = MutableStateFlow(THEME_MODE_DEFAULT)
            every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOn
            every { dynamicColorRepository.dynamicColorEnabled() } returns dynamicColor
            every { hapticFeedbackRepository.hapticFeedbackEnabled() } returns hapticFeedback
            every { themeRepository.observeThemeMode() } returns themeMode
            val flow = createFlow()

            assertEquals(
                OtherListDeviceSettingsUiState(false, false, false, THEME_MODE_DEFAULT),
                flow.uiState.first(),
            )
            val updatedState =
                async(UnconfinedTestDispatcher(testScheduler)) {
                    flow.uiState.first {
                        it == OtherListDeviceSettingsUiState(true, true, true, ThemeMode.DARK)
                    }
                }

            keepScreenOn.value = true
            dynamicColor.value = true
            hapticFeedback.value = true
            themeMode.value = ThemeMode.DARK

            assertEquals(
                OtherListDeviceSettingsUiState(true, true, true, ThemeMode.DARK),
                updatedState.await(),
            )
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { themeRepository.observeThemeMode() }
            confirmVerified(keepScreenOnRepository, dynamicColorRepository, hapticFeedbackRepository, themeRepository)
        }

    @Test
    fun `保存操作を各UseCaseに委譲する`() =
        runTest {
            every { keepScreenOnRepository.keepScreenOn() } returns MutableStateFlow(true)
            every { dynamicColorRepository.dynamicColorEnabled() } returns MutableStateFlow(false)
            every { hapticFeedbackRepository.hapticFeedbackEnabled() } returns MutableStateFlow(true)
            every { themeRepository.observeThemeMode() } returns MutableStateFlow(THEME_MODE_DEFAULT)
            coEvery { keepScreenOnRepository.saveKeepScreenOn(true) } returns Unit
            coEvery { dynamicColorRepository.saveDynamicColorEnabled(false) } returns Unit
            coEvery { hapticFeedbackRepository.saveHapticFeedbackEnabled(true) } returns Unit
            val flow = createFlow()

            flow.onKeepScreenOnChange(true)
            flow.onDynamicColorEnabledChange(false)
            flow.onHapticFeedbackEnabledChange(true)

            coVerify(exactly = 1) { keepScreenOnRepository.saveKeepScreenOn(true) }
            coVerify(exactly = 1) { dynamicColorRepository.saveDynamicColorEnabled(false) }
            coVerify(exactly = 1) { hapticFeedbackRepository.saveHapticFeedbackEnabled(true) }
            verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
            verify(exactly = 1) { dynamicColorRepository.dynamicColorEnabled() }
            verify(exactly = 1) { hapticFeedbackRepository.hapticFeedbackEnabled() }
            verify(exactly = 1) { themeRepository.observeThemeMode() }
            confirmVerified(keepScreenOnRepository, dynamicColorRepository, hapticFeedbackRepository, themeRepository)
        }

    private fun createFlow() =
        OtherListDeviceSettingsFlow(
            ObserveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
            SaveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
            ObserveDynamicColorEnabledUseCase(dynamicColorRepository),
            SaveDynamicColorEnabledUseCase(dynamicColorRepository),
            ObserveHapticFeedbackEnabledUseCase(hapticFeedbackRepository),
            SaveHapticFeedbackEnabledUseCase(hapticFeedbackRepository),
            ObserveThemeModeUseCase(themeRepository),
        )
}
