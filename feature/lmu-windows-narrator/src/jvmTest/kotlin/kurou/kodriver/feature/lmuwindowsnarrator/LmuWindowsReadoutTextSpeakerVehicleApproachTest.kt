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
import kurou.kodriver.domain.engine.LmuWindowsCarLeft
import kurou.kodriver.domain.engine.LmuWindowsCarLeftSustained
import kurou.kodriver.domain.engine.LmuWindowsCarRight
import kurou.kodriver.domain.engine.LmuWindowsCarRightSustained
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBlueFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsBrakeWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsFullCourseYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsMyBestLapReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingTyreWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyImminentReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsPitTimingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRedFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsRemainingVirtualEnergyReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsSectorYellowFlagReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsTyreWearReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachStartRightReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase
import kurou.kodriver.domain.lmuwindows.usecase.ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase
import kurou.kodriver.domain.usecase.CheckTextToSpeechAvailableUseCase
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
    private val observeBrakeText: ObserveLmuWindowsBrakeTemperatureReadoutTextUseCase = mockk()
    private val observeBrakeWearText: ObserveLmuWindowsBrakeWearReadoutTextUseCase = mockk()
    private val observeTyreWearText: ObserveLmuWindowsTyreWearReadoutTextUseCase = mockk()
    private val observeTyreOverheatReadoutText: ObserveLmuWindowsTyreTemperatureOverheatReadoutTextUseCase = mockk()
    private val observeTyreColdReadoutText: ObserveLmuWindowsTyreTemperatureColdReadoutTextUseCase = mockk()
    private val observeOverheatReadoutText: ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase = mockk()
    private val observePartDetachedReadoutText: ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase = mockk()
    private val observeTyreDetachedReadoutText: ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase = mockk()
    private val observeMyBestLapReadoutText: ObserveLmuWindowsMyBestLapReadoutTextUseCase = mockk()
    private val checkTextToSpeechAvailable: CheckTextToSpeechAvailableUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val speaker =
        LmuWindowsReadoutTextSpeaker(
            readoutTexts =
                LmuWindowsReadoutTextUseCases(
                    flag =
                        LmuWindowsFlagReadoutTextUseCases(
                            sectorYellow = observeSectorYellow,
                            blue = observeBlue,
                            fullCourseYellow = observeFullCourseYellow,
                            red = observeRed,
                        ),
                    vehicleApproach =
                        LmuWindowsVehicleApproachReadoutTextUseCases(
                            startLeft = observeLeft,
                            startRight = observeRight,
                            sustainedLeft = observeSustainedLeft,
                            sustainedRight = observeSustainedRight,
                        ),
                    pitTiming =
                        LmuWindowsPitTimingReadoutTextUseCases(
                            virtualEnergy = observePitTimingVirtualEnergyReadoutText,
                            virtualEnergyImminent = observePitTimingVirtualEnergyImminentReadoutText,
                            tyreWear = observePitTimingTyreWearReadoutText,
                            tyreWearImminent = observePitTimingTyreWearImminentReadoutText,
                        ),
                    tyre =
                        LmuWindowsTyreReadoutTextUseCases(
                            wear = observeTyreWearText,
                            overheat = observeTyreOverheatReadoutText,
                            cold = observeTyreColdReadoutText,
                        ),
                    vehicleDamage =
                        LmuWindowsVehicleDamageReadoutTextUseCases(
                            overheat = observeOverheatReadoutText,
                            partDetached = observePartDetachedReadoutText,
                            tyreDetached = observeTyreDetachedReadoutText,
                        ),
                    brake =
                        LmuWindowsBrakeReadoutTextUseCases(
                            temperature = observeBrakeText,
                            wear = observeBrakeWearText,
                        ),
                    remainingVirtualEnergy = observeRemainingText,
                    myBestLap = observeMyBestLapReadoutText,
                ),
            checkTextToSpeechAvailable = checkTextToSpeechAvailable,
            speakText = speakText,
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
            observeBrakeText,
            observeBrakeWearText,
            observeTyreWearText,
            observeTyreOverheatReadoutText,
            observeTyreColdReadoutText,
            observeOverheatReadoutText,
            observePartDetachedReadoutText,
            observeTyreDetachedReadoutText,
            observeMyBestLapReadoutText,
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

            speaker(LmuWindowsCarLeft(), VOLUME)

            verify(exactly = 1) { observeLeft() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("左注意", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `Leftの文言は空白なら読み上げない`() =
        runTest {
            every { observeLeft() } returns flowOf("  ")

            speaker(LmuWindowsCarLeft(), VOLUME)

            verify(exactly = 1) { observeLeft() }
            confirmAllMocksVerified()
        }

    @Test
    fun `Leftの文言はTTS不可なら読み上げない`() =
        runTest {
            every { observeLeft() } returns flowOf("左注意")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(LmuWindowsCarLeft(), VOLUME)

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

            speaker(LmuWindowsCarRight(), VOLUME)

            verify(exactly = 1) { observeRight() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("右注意", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `Rightの文言は空白なら読み上げない`() =
        runTest {
            every { observeRight() } returns flowOf("  ")

            speaker(LmuWindowsCarRight(), VOLUME)

            verify(exactly = 1) { observeRight() }
            confirmAllMocksVerified()
        }

    @Test
    fun `Rightの文言はTTS不可なら読み上げない`() =
        runTest {
            every { observeRight() } returns flowOf("右注意")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(LmuWindowsCarRight(), VOLUME)

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

            speaker(LmuWindowsCarLeftSustained(), VOLUME)

            verify(exactly = 1) { observeSustainedLeft() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("左注意", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `継続Leftの文言は空白なら読み上げない`() =
        runTest {
            every { observeSustainedLeft() } returns flowOf("  ")

            speaker(LmuWindowsCarLeftSustained(), VOLUME)

            verify(exactly = 1) { observeSustainedLeft() }
            confirmAllMocksVerified()
        }

    @Test
    fun `継続Leftの文言はTTS不可なら読み上げない`() =
        runTest {
            every { observeSustainedLeft() } returns flowOf("左注意")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(LmuWindowsCarLeftSustained(), VOLUME)

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

            speaker(LmuWindowsCarRightSustained(), VOLUME)

            verify(exactly = 1) { observeSustainedRight() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            coVerify(exactly = 1) { speakText("右注意", volume = VOLUME) }
            confirmAllMocksVerified()
        }

    @Test
    fun `継続Rightの文言は空白なら読み上げない`() =
        runTest {
            every { observeSustainedRight() } returns flowOf("  ")

            speaker(LmuWindowsCarRightSustained(), VOLUME)

            verify(exactly = 1) { observeSustainedRight() }
            confirmAllMocksVerified()
        }

    @Test
    fun `継続Rightの文言はTTS不可なら読み上げない`() =
        runTest {
            every { observeSustainedRight() } returns flowOf("右注意")
            coEvery { checkTextToSpeechAvailable() } returns false

            speaker(LmuWindowsCarRightSustained(), VOLUME)

            verify(exactly = 1) { observeSustainedRight() }
            coVerify(exactly = 1) { checkTextToSpeechAvailable() }
            confirmAllMocksVerified()
        }

    private companion object {
        const val VOLUME = 40
    }
}
