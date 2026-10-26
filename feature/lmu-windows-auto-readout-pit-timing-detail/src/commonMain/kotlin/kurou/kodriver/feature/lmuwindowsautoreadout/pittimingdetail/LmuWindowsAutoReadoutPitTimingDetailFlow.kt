package kurou.kodriver.feature.lmuwindowsautoreadout.pittimingdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

/** ピットタイミング設定と画面内状態を合成する冷たい Flow。 */
@Suppress("LongParameterList")
internal class LmuWindowsAutoReadoutPitTimingDetailFlow(
    virtualEnergyLaps: Flow<Int>,
    tyreWearLaps: Flow<Int>,
    fuelLaps: Flow<Int>,
    enabledStates: Flow<Map<ReadoutItemKey, Boolean>>,
    virtualEnergyText: Flow<String>,
    virtualEnergyImminentText: Flow<String>,
    tyreWearText: Flow<String>,
    tyreWearImminentText: Flow<String>,
    fuelText: Flow<String>,
    fuelImminentText: Flow<String>,
    textToSpeechAvailable: Flow<Boolean>,
) {
    val uiState: Flow<LmuWindowsAutoReadoutPitTimingDetailUiState> =
        combine(
            virtualEnergyLaps,
            combine(tyreWearLaps, fuelLaps) { tyre, fuel -> tyre to fuel },
            enabledStates,
            combine(
                combine(virtualEnergyText, virtualEnergyImminentText) { text, imminent -> text to imminent },
                combine(tyreWearText, tyreWearImminentText) { text, imminent -> text to imminent },
                combine(fuelText, fuelImminentText) { text, imminent -> text to imminent },
            ) { virtualEnergy, tyreWear, fuel -> Triple(virtualEnergy, tyreWear, fuel) },
            textToSpeechAvailable,
        ) { virtualEnergyLaps, tyreWearLaps, enabledStates, texts, available ->
            LmuWindowsAutoReadoutPitTimingDetailUiState(
                virtualEnergyEnabled = enabledStates.getValue(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy),
                virtualEnergyLaps = virtualEnergyLaps,
                virtualEnergyText = texts.first.first,
                virtualEnergyImminentText = texts.first.second,
                isTextToSpeechAvailable = available,
                tyreWearEnabled = enabledStates.getValue(LmuWindowsReadoutItemKey.PitTiming.TyreWear),
                tyreWearLaps = tyreWearLaps.first,
                fuelEnabled = enabledStates.getValue(LmuWindowsReadoutItemKey.PitTiming.Fuel),
                fuelLaps = tyreWearLaps.second,
                fuelText = texts.third.first,
                fuelImminentText = texts.third.second,
                tyreWearText = texts.second.first,
                tyreWearImminentText = texts.second.second,
            )
        }
}
