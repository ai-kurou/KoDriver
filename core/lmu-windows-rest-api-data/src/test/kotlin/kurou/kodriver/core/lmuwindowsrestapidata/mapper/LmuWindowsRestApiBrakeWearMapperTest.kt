package kurou.kodriver.core.lmuwindowsrestapidata.mapper

import kurou.kodriver.core.lmuwindowsrestapidata.dto.RepairAndRefuelResponseDto
import kurou.kodriver.core.lmuwindowsrestapidata.dto.WearablesDto
import kurou.kodriver.domain.model.BrakeThicknessMeters
import kurou.kodriver.domain.model.WheelIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LmuWindowsRestApiBrakeWearMapperTest {
    @Test
    fun `toBrakeWearは4輪分の配列をFL_FR_RL_RRの順でホイールへ割り当てる`() {
        val dto = RepairAndRefuelResponseDto(WearablesDto(brakes = listOf(0.036, 0.035, 0.032, 0.031)))

        val result = LmuWindowsRestApiBrakeWearMapper.toBrakeWear(dto)

        assertEquals(
            mapOf(
                WheelIndex.FRONT_LEFT to BrakeThicknessMeters(0.036f),
                WheelIndex.FRONT_RIGHT to BrakeThicknessMeters(0.035f),
                WheelIndex.REAR_LEFT to BrakeThicknessMeters(0.032f),
                WheelIndex.REAR_RIGHT to BrakeThicknessMeters(0.031f),
            ),
            result?.wheels,
        )
    }

    @Test
    fun `toBrakeWearはwearablesが無ければnullを返す`() {
        assertNull(LmuWindowsRestApiBrakeWearMapper.toBrakeWear(RepairAndRefuelResponseDto()))
    }

    @Test
    fun `toBrakeWearはbrakesが無ければnullを返す`() {
        val dto = RepairAndRefuelResponseDto(WearablesDto())

        assertNull(LmuWindowsRestApiBrakeWearMapper.toBrakeWear(dto))
    }

    @Test
    fun `toBrakeWearはbrakesが4輪分でなければnullを返す`() {
        val dto = RepairAndRefuelResponseDto(WearablesDto(brakes = listOf(0.036, 0.035)))

        assertNull(LmuWindowsRestApiBrakeWearMapper.toBrakeWear(dto))
    }
}
