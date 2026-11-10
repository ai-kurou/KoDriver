package kurou.kodriver.feature.lmuwindowsautoreadout.vehicleapproachdetail

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.captureError
import kurou.kodriver.domain.lmuwindows.usecase.LmuWindowsVehicleApproachPreferencesUseCases
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import java.io.IOException
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutTextSettingsStateHolderTest {
    private val preferences: LmuWindowsVehicleApproachPreferencesUseCases = mockk()
    private val observeSustainedLeft: ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase = mockk()
    private val observeSustainedRight: ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase = mockk()
    private val saveStartLeft: SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase = mockk()
    private val saveStartRight: SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase = mockk()
    private val checkAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val observeVolume: ObserveSoundVolumeUseCase = mockk()
    private val playStartSound: PlayStartSoundForKeyUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()

    private fun TestScope.createHolder() =
        ReadoutTextSettingsStateHolder(
            scope = backgroundScope,
            vehicleApproachPreferences = preferences,
            observeSustainedLeftText = observeSustainedLeft,
            observeSustainedRightText = observeSustainedRight,
            saveStartLeftText = saveStartLeft,
            saveStartRightText = saveStartRight,
            checkTextToSpeechAvailable = checkAvailable,
            observeSoundVolume = observeVolume,
            playStartSoundForKey = playStartSound,
            speakText = speakText,
        )

    private fun stubObservations() {
        coEvery { checkAvailable() } returns false
        every { preferences.observeStartLeftReadoutText() } returns flowOf("左開始")
        every { preferences.observeStartRightReadoutText() } returns flowOf("右開始")
        every { observeSustainedLeft() } returns flowOf("左継続")
        every { observeSustainedRight() } returns flowOf("右継続")
    }

    private fun verifyObservations() {
        verify(exactly = 1) { preferences.observeStartLeftReadoutText() }
        verify(exactly = 1) { preferences.observeStartRightReadoutText() }
        verify(exactly = 1) { observeSustainedLeft() }
        verify(exactly = 1) { observeSustainedRight() }
    }

    @Test
    fun `開始左の保存が失敗しても記録して例外を伝播させない`() =
        runTest {
            val error = IOException("fail")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                stubObservations()
                coEvery { saveStartLeft("新しい左") } throws error
                val holder = createHolder()

                holder.onTextChanged(VehicleApproachReadoutTextSlot.StartLeft, "新しい左")
                runCurrent()

                coVerify(exactly = 1) { saveStartLeft("新しい左") }
                verify(exactly = 1) { captureError(error) }
                verifyObservations()
                confirmVerified(errorCapture, preferences, observeSustainedLeft, observeSustainedRight, saveStartLeft)
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `開始右の保存が失敗しても記録して例外を伝播させない`() =
        runTest {
            val error = IOException("fail")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                stubObservations()
                coEvery { saveStartRight("新しい右") } throws error
                val holder = createHolder()

                holder.onTextChanged(VehicleApproachReadoutTextSlot.StartRight, "新しい右")
                runCurrent()

                coVerify(exactly = 1) { saveStartRight("新しい右") }
                verify(exactly = 1) { captureError(error) }
                verifyObservations()
                confirmVerified(errorCapture, preferences, observeSustainedLeft, observeSustainedRight, saveStartRight)
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `継続左の保存が失敗しても記録して例外を伝播させない`() =
        runTest {
            val error = IOException("fail")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                stubObservations()
                coEvery { preferences.saveSustainedLeftReadoutText("新しい継続左") } throws error
                val holder = createHolder()

                holder.onTextChanged(VehicleApproachReadoutTextSlot.SustainedLeft, "新しい継続左")
                runCurrent()

                coVerify(exactly = 1) { preferences.saveSustainedLeftReadoutText("新しい継続左") }
                verify(exactly = 1) { captureError(error) }
                verifyObservations()
                confirmVerified(errorCapture, preferences, observeSustainedLeft, observeSustainedRight)
            } finally {
                unmockkStatic(errorCapture)
            }
        }

    @Test
    fun `継続右の保存が失敗しても記録して例外を伝播させない`() =
        runTest {
            val error = IOException("fail")
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                stubObservations()
                coEvery { preferences.saveSustainedRightReadoutText("新しい継続右") } throws error
                val holder = createHolder()

                holder.onTextChanged(VehicleApproachReadoutTextSlot.SustainedRight, "新しい継続右")
                runCurrent()

                coVerify(exactly = 1) { preferences.saveSustainedRightReadoutText("新しい継続右") }
                verify(exactly = 1) { captureError(error) }
                verifyObservations()
                confirmVerified(errorCapture, preferences, observeSustainedLeft, observeSustainedRight)
            } finally {
                unmockkStatic(errorCapture)
            }
        }
}
