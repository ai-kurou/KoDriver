package kurou.kodriver.feature.manualreadoutdetail

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.Res
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_brake_temperature
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_brake_wear
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_class_car_gap_ahead
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_class_car_gap_behind
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_energy
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_fuel_laps
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_my_best_lap
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_remaining_fuel
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_remaining_fuel_laps
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_tyre_temperature
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_tyre_wear
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_tyre_wear_laps
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_virtual_energy_laps
import org.jetbrains.compose.resources.StringResource

/** カテゴリ・名称は画面内の表示用メタデータとして管理する。 */
internal data class ManualReadoutItemPresentation(
    val name: StringResource,
)

internal val itemPresentations =
    mapOf(
        LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_virtual_energy_laps,
            ),
        LmuWindowsReadoutItemKey.RemainingFuel.Laps.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_fuel_laps,
            ),
        LmuWindowsReadoutItemKey.ClassCarGap.Ahead.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_class_car_gap_ahead,
            ),
        LmuWindowsReadoutItemKey.ClassCarGap.Behind.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_class_car_gap_behind,
            ),
        LmuWindowsReadoutItemKey.PitTiming.TyreWear.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_tyre_wear_laps,
            ),
        LmuWindowsReadoutItemKey.MyBestLap.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_my_best_lap,
            ),
        AceWindowsReadoutItemKey.MyBestLap.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_my_best_lap,
            ),
        LmuWindowsReadoutItemKey.RemainingFuel.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_remaining_fuel,
            ),
        AceWindowsReadoutItemKey.RemainingFuel.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_remaining_fuel,
            ),
        AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_remaining_fuel_laps,
            ),
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_remaining_fuel,
            ),
        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_remaining_fuel_laps,
            ),
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_energy,
            ),
        Gt7Ps5ReadoutItemKey.MyBestLap.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_my_best_lap,
            ),
        AceWindowsReadoutItemKey.TyreTemperature.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_tyre_temperature,
            ),
        Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_tyre_temperature,
            ),
        LmuWindowsReadoutItemKey.TyreWear.Root.value to
            ManualReadoutItemPresentation(Res.string.manual_readout_tyre_wear),
        LmuWindowsReadoutItemKey.BrakeWear.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_brake_wear,
            ),
        LmuWindowsReadoutItemKey.BrakeTemperature.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_brake_temperature,
            ),
        LmuWindowsReadoutItemKey.TyreTemperature.Root.value to
            ManualReadoutItemPresentation(
                Res.string.manual_readout_tyre_temperature,
            ),
    )
