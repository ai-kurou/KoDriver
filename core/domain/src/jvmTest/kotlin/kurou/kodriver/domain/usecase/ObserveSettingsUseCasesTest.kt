package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.model.MainWindowBounds
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.repository.LowVolumeWarningEnabledPreferencesRepository
import kurou.kodriver.domain.repository.MainWindowBoundsPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository
import kurou.kodriver.domain.repository.VoicePreferencesRepository
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSettingsUseCasesTest {
    @Test
    fun `Observe系UseCaseはRepositoryの監視Flowの値をそのまま返す1`() {
        assertObserves<DynamicColorEnabledRepository, Boolean>(
            "ObserveDynamicColorEnabledUseCase",
            true,
            observe = { dynamicColorEnabled() },
            useCase = { ObserveDynamicColorEnabledUseCase(it)() },
        )
        assertObserves<KeepScreenOnEnabledRepository, Boolean>(
            "ObserveKeepScreenOnEnabledUseCase",
            true,
            observe = { keepScreenOn() },
            useCase = { ObserveKeepScreenOnEnabledUseCase(it)() },
        )
        assertObserves<AndroidAudioUsagePreferencesRepository, AndroidAudioUsage>(
            "ObserveAndroidAudioUsageUseCase",
            AndroidAudioUsage.ALARM,
            observe = { observeAndroidAudioUsage() },
            useCase = { ObserveAndroidAudioUsageUseCase(it)() },
        )
        assertObserves<DeviceVolumeRepository, Int>(
            "ObserveDeviceVolumeUseCase",
            42,
            observe = { observeVolume() },
            useCase = { ObserveDeviceVolumeUseCase(it)() },
        )
        assertObserves<ReadoutStartSoundPreferencesRepository, ReadoutStartSoundType>(
            "ObserveReadoutStartSoundTypeUseCase",
            ReadoutStartSoundType.FORMULA_RADIO,
            observe = { observeType() },
            useCase = { ObserveReadoutStartSoundTypeUseCase(it)() },
        )
        assertObserves<ThemePreferencesRepository, ThemeMode>(
            "ObserveThemeModeUseCase",
            ThemeMode.DARK,
            observe = { observeThemeMode() },
            useCase = { ObserveThemeModeUseCase(it)() },
        )
    }

    @Test
    fun `Observe系UseCaseはRepositoryの監視Flowの値をそのまま返す2`() {
        assertObserves<LowVolumeWarningEnabledPreferencesRepository, Boolean>(
            "ObserveLowVolumeWarningEnabledUseCase",
            true,
            observe = { observeLowVolumeWarningEnabled() },
            useCase = { ObserveLowVolumeWarningEnabledUseCase(it)() },
        )
        assertObserves<SimulatorPreferencesRepository, Simulator>(
            "ObserveSelectedSimulatorUseCase",
            Simulator.Gt7Ps5,
            observe = { selectedSimulator() },
            useCase = { ObserveSelectedSimulatorUseCase(it)() },
        )
    }

    @Test
    fun `Observe系UseCaseはRepositoryの監視Flowの値をそのまま返す3`() {
        assertObserves<MainWindowBoundsPreferencesRepository, MainWindowBounds>(
            "ObserveMainWindowBoundsUseCase",
            MainWindowBounds(x = 10, y = 20, width = 800, height = 500),
            observe = { observeMainWindowBounds() },
            useCase = { ObserveMainWindowBoundsUseCase(it)() },
        )
        assertObserves<SoundVolumePreferencesRepository, Int>(
            "ObserveSoundVolumeUseCase",
            80,
            observe = { volume() },
            useCase = { ObserveSoundVolumeUseCase(it)() },
        )
        assertObserves<VoicePitchPreferencesRepository, Float>(
            "ObserveVoicePitchUseCase",
            1.0f,
            observe = { voicePitch() },
            useCase = { ObserveVoicePitchUseCase(it)() },
        )
        assertObserves<VoiceSpeedPreferencesRepository, Float>(
            "ObserveVoiceSpeedUseCase",
            1.25f,
            observe = { voiceSpeed() },
            useCase = { ObserveVoiceSpeedUseCase(it)() },
        )
        assertObserves<VoicePreferencesRepository, String>(
            "ObserveVoiceUseCase",
            "ja-jp-x-jab-local",
            observe = { voiceId() },
            useCase = { ObserveVoiceUseCase(it)() },
        )
    }

    private inline fun <reified R : Any, T> assertObserves(
        useCaseName: String,
        expected: T,
        crossinline observe: R.() -> Flow<T>,
        crossinline useCase: (R) -> Flow<T>,
    ) {
        val repository = mockk<R>()
        every { repository.observe() } returns flowOf(expected)
        try {
            runTest {
                assertEquals(expected, useCase(repository).first())
            }
            verify(exactly = 1) { repository.observe() }
            confirmVerified(repository)
        } catch (e: AssertionError) {
            throw AssertionError("$useCaseName: ${e.message}", e)
        }
    }
}
