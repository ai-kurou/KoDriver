package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.SpeechCommandGuideGroup
import kurou.kodriver.domain.model.SpeechCommandGuideItem
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SpeechCommandGuideRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveSpeechCommandGuideUseCaseTest {
    private val simulatorRepository: SimulatorPreferencesRepository = mockk()
    private val guideRepository: SpeechCommandGuideRepository = mockk()

    private val lmuGuide =
        listOf(
            SpeechCommandGuideGroup(
                ManualReadoutItemCategory.FUEL,
                listOf(SpeechCommandGuideItem("fuel", "燃料残量", listOf("燃料", "ガソリン"))),
            ),
        )
    private val gt7Guide = emptyList<SpeechCommandGuideGroup>()

    @Test
    fun `選択中のシミュレーターの案内を返しシミュレーターの切り替えに追従する`() =
        runTest {
            every { simulatorRepository.selectedSimulator() } returns flowOf(Simulator.LmuWindows, Simulator.Gt7Ps5)
            every { guideRepository.guideGroups(Simulator.LmuWindows) } returns lmuGuide
            every { guideRepository.guideGroups(Simulator.Gt7Ps5) } returns gt7Guide

            val emitted = ObserveSpeechCommandGuideUseCase(simulatorRepository, guideRepository)().toList()

            assertEquals(listOf(lmuGuide, gt7Guide), emitted)
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            verify(exactly = 1) { guideRepository.guideGroups(Simulator.LmuWindows) }
            verify(exactly = 1) { guideRepository.guideGroups(Simulator.Gt7Ps5) }
            confirmVerified(simulatorRepository, guideRepository)
        }
}
