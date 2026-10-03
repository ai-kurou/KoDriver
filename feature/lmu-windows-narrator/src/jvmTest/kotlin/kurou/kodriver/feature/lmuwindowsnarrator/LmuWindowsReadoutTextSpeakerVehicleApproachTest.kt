package kurou.kodriver.feature.lmuwindowsnarrator

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.engine.SpeechEvent
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase
import kurou.kodriver.domain.usecase.ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import kotlin.test.Test

class LmuWindowsReadoutTextSpeakerVehicleApproachTest {
    private val observeSectorYellow: ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase = mockk()
    private val observeBlue: ObserveLmuWindowsBlueFlagReadoutTextUseCase = mockk()
    private val observeFullCourseYellow: ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase = mockk()
    private val observeRed: ObserveLmuWindowsRedFlagReadoutTextUseCase = mockk()
    private val observeLeft: ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase = mockk()
    private val observeRight: ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase = mockk()
    private val observeSustainedLeft: ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase = mockk()
    private val observeSustainedRight: ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase = mockk()
    private val observePitTimingVirtualEnergyReadoutText: ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase =
        mockk()
    private val observePitTimingVirtualEnergyImminentReadoutText:
        ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase = mockk()
    private val observePitTimingTyreWearReadoutText: ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase = mockk()
    private val observePitTimingTyreWearImminentReadoutText:
        ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase = mockk()
    private val observeRemainingText: ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase = mockk()
    private val observeTyreOverheatReadoutText: ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase = mockk()
    private val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val speaker =
        LmuWindowsReadoutTextSpeaker(
            observeSectorYellow,
            observeBlue,
            observeFullCourseYellow,
            observeRed,
            observeLeft,
            observeRight,
            observeSustainedLeft,
            observeSustainedRight,
            observePitTimingVirtualEnergyReadoutText,
            observePitTimingVirtualEnergyImminentReadoutText,
            observePitTimingTyreWearReadoutText,
            observePitTimingTyreWearImminentReadoutText,
            observeRemainingText,
            observeTyreOverheatReadoutText,
            checkTextToSpeechAvailable,
            speakText,
        )

    private fun confirmAllMocksVerified() {
        confirmVerified(
            observeSectorYellow,
            observeBlue,
            observeFullCourseYellow,
            observeRed,
            observeLeft,
            observeRight,
            observeSustainedLeft,
            observeSustainedRight,
            observePitTimingVirtualEnergyReadoutText,
            observePitTimingVirtualEnergyImminentReadoutText,
            observePitTimingTyreWearReadoutText,
            observePitTimingTyreWearImminentReadoutText,
            observeRemainingText,
            observeTyreOverheatReadoutText,
            checkTextToSpeechAvailable,
            speakText,
        )
    }

    @Test
    fun `Leftの文言はTTSで読み上げる`() =
        runTest {
            every { observeLeft() } returns flowOf("左注意")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("左注意", volume = VOLUME) } just Runs

            speaker(SpeechEvent.CarLeft, VOLUME)

            verify(exactly = 1) { observeLeft() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("左注意", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `Leftの文言は空白なら読み上げない`() =
        runTest {
            every { observeLeft() } returns flowOf("  ")

            speaker(SpeechEvent.CarLeft, VOLUME)

            verify(exactly = 1) { observeLeft() }
            confirmAllMocksVerified()
        }

    @Test
    fun `Leftの文言はTTS不可なら読み上げない`() =
        runTest {
            every { observeLeft() } returns flowOf("左注意")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(SpeechEvent.CarLeft, VOLUME)

            verify(exactly = 1) { observeLeft() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `Rightの文言はTTSで読み上げる`() =
        runTest {
            every { observeRight() } returns flowOf("右注意")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("右注意", volume = VOLUME) } just Runs

            speaker(SpeechEvent.CarRight, VOLUME)

            verify(exactly = 1) { observeRight() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("右注意", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `Rightの文言は空白なら読み上げない`() =
        runTest {
            every { observeRight() } returns flowOf("  ")

            speaker(SpeechEvent.CarRight, VOLUME)

            verify(exactly = 1) { observeRight() }
            confirmAllMocksVerified()
        }

    @Test
    fun `Rightの文言はTTS不可なら読み上げない`() =
        runTest {
            every { observeRight() } returns flowOf("右注意")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(SpeechEvent.CarRight, VOLUME)

            verify(exactly = 1) { observeRight() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `継続Leftの文言はTTSで読み上げる`() =
        runTest {
            every { observeSustainedLeft() } returns flowOf("左注意")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("左注意", volume = VOLUME) } just Runs

            speaker(SpeechEvent.CarLeftSustained, VOLUME)

            verify(exactly = 1) { observeSustainedLeft() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("左注意", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `継続Leftの文言は空白なら読み上げない`() =
        runTest {
            every { observeSustainedLeft() } returns flowOf("  ")

            speaker(SpeechEvent.CarLeftSustained, VOLUME)

            verify(exactly = 1) { observeSustainedLeft() }
            confirmAllMocksVerified()
        }

    @Test
    fun `継続Leftの文言はTTS不可なら読み上げない`() =
        runTest {
            every { observeSustainedLeft() } returns flowOf("左注意")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(SpeechEvent.CarLeftSustained, VOLUME)

            verify(exactly = 1) { observeSustainedLeft() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    @Test
    fun `継続Rightの文言はTTSで読み上げる`() =
        runTest {
            every { observeSustainedRight() } returns flowOf("右注意")
            coEvery { checkTextToSpeechAvailable() } returns true
            coEvery { speakText("右注意", volume = VOLUME) } just Runs

            speaker(SpeechEvent.CarRightSustained, VOLUME)

            verify(exactly = 1) { observeSustainedRight() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("右注意", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `継続Rightの文言は空白なら読み上げない`() =
        runTest {
            every { observeSustainedRight() } returns flowOf("  ")

            speaker(SpeechEvent.CarRightSustained, VOLUME)

            verify(exactly = 1) { observeSustainedRight() }
            confirmAllMocksVerified()
        }

    @Test
    fun `継続Rightの文言はTTS不可なら読み上げない`() =
        runTest {
            every { observeSustainedRight() } returns flowOf("右注意")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(SpeechEvent.CarRightSustained, VOLUME)

            verify(exactly = 1) { observeSustainedRight() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    private companion object {
        const val VOLUME = 40
    }
}
