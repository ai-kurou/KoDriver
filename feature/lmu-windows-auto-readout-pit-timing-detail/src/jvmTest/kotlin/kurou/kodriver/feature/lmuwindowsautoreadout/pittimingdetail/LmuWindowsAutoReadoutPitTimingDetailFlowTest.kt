package kurou.kodriver.feature.lmuwindowsautoreadout.pittimingdetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsAutoReadoutPitTimingDetailFlowTest {
    @Test
    fun `入力Flowの値を各設定状態へ反映する`() =
        runTest {
            val virtualEnergyLaps = MutableStateFlow(3)
            val tyreWearLaps = MutableStateFlow(3)
            val fuelLaps = MutableStateFlow(3)
            val enabledStates =
                MutableStateFlow<Map<ReadoutItemKey, Boolean>>(
                    mapOf(
                        LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy to true,
                        LmuWindowsReadoutItemKey.PitTiming.TyreWear to true,
                        LmuWindowsReadoutItemKey.PitTiming.Fuel to true,
                    ),
                )
            val virtualEnergyText = MutableStateFlow("残り{laps}周")
            val virtualEnergyImminentText = MutableStateFlow("必ずピットイン")
            val tyreWearText = MutableStateFlow("タイヤ残り{laps}周")
            val tyreWearImminentText = MutableStateFlow("タイヤ交換へ")
            val fuelText = MutableStateFlow("燃料残り{laps}周")
            val fuelImminentText = MutableStateFlow("給油へ")
            val textToSpeechAvailable = MutableStateFlow(false)
            val flow =
                LmuWindowsAutoReadoutPitTimingDetailFlow(
                    virtualEnergyLaps = virtualEnergyLaps,
                    tyreWearLaps = tyreWearLaps,
                    fuelLaps = fuelLaps,
                    enabledStates = enabledStates,
                    virtualEnergyText = virtualEnergyText,
                    virtualEnergyImminentText = virtualEnergyImminentText,
                    tyreWearText = tyreWearText,
                    tyreWearImminentText = tyreWearImminentText,
                    fuelText = fuelText,
                    fuelImminentText = fuelImminentText,
                    textToSpeechAvailable = textToSpeechAvailable,
                )

            assertEquals(
                LmuWindowsAutoReadoutPitTimingDetailUiState(
                    virtualEnergyText = "残り{laps}周",
                    virtualEnergyImminentText = "必ずピットイン",
                    tyreWearText = "タイヤ残り{laps}周",
                    tyreWearImminentText = "タイヤ交換へ",
                    fuelText = "燃料残り{laps}周",
                    fuelImminentText = "給油へ",
                ),
                flow.uiState.first(),
            )

            virtualEnergyLaps.value = 4
            tyreWearLaps.value = 2
            fuelLaps.value = 1
            enabledStates.update {
                it +
                    (LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy to false) +
                    (LmuWindowsReadoutItemKey.PitTiming.TyreWear to false) +
                    (LmuWindowsReadoutItemKey.PitTiming.Fuel to false)
            }
            virtualEnergyText.value = "VE {laps}周"
            virtualEnergyImminentText.value = "VE ピット"
            tyreWearText.value = "タイヤ {laps}周"
            tyreWearImminentText.value = "タイヤ ピット"
            fuelText.value = "燃料 {laps}周"
            fuelImminentText.value = "燃料 ピット"
            textToSpeechAvailable.value = true

            assertEquals(
                LmuWindowsAutoReadoutPitTimingDetailUiState(
                    virtualEnergyEnabled = false,
                    virtualEnergyLaps = 4,
                    virtualEnergyText = "VE {laps}周",
                    virtualEnergyImminentText = "VE ピット",
                    isTextToSpeechAvailable = true,
                    tyreWearEnabled = false,
                    fuelEnabled = false,
                    tyreWearLaps = 2,
                    fuelLaps = 1,
                    tyreWearText = "タイヤ {laps}周",
                    fuelText = "燃料 {laps}周",
                    tyreWearImminentText = "タイヤ ピット",
                    fuelImminentText = "燃料 ピット",
                ),
                flow.uiState.first(),
            )
        }
}
