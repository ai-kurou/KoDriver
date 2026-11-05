package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.engine.Gt7Ps5MyBestLap
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelCurrentValue
import kurou.kodriver.domain.engine.Gt7Ps5RemainingFuelLapsCurrentValue
import kurou.kodriver.domain.engine.Gt7Ps5TyreTemperatureCurrentValue
import kurou.kodriver.domain.engine.ReadoutTextEvent
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.formatGt7Ps5MyBestLapReadoutText
import kurou.kodriver.domain.gt7ps5.model.formatGt7Ps5RemainingFuelLapsReadoutText
import kurou.kodriver.domain.gt7ps5.model.formatGt7Ps5RemainingFuelReadoutText
import kurou.kodriver.domain.gt7ps5.model.formatGt7Ps5TyreTemperatureReadoutText
import kurou.kodriver.domain.gt7ps5.model.gt7Ps5ManualReadoutItemName
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.Gt7Ps5ManualReadoutItem
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutValue
import kurou.kodriver.domain.model.manualReadoutNoDataText

/** GT7 の手動読み上げセットの各項目から、読み上げイベントを組み立てる。 */
internal object Gt7Ps5ManualReadoutEvents {
    /** 項目キーに対応する種別の値だけを取り出す。値が無い、または種別が対応しない場合は null（データなし）。 */
    private fun manualValue(item: Gt7Ps5ManualReadoutItem): Int? =
        when (item.key) {
            Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value -> {
                (item.value as? ManualReadoutValue.Percent)?.value
            }

            Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value -> {
                (item.value as? ManualReadoutValue.Celsius)?.value
            }

            Gt7Ps5ReadoutItemKey.MyBestLap.Root.value -> {
                (item.value as? ManualReadoutValue.LapTimeMs)?.value
            }

            Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value -> {
                (item.value as? ManualReadoutValue.RemainingLaps)
                    ?.value
            }

            else -> {
                null
            }
        }

    /** 手動読み上げの対応外キーは null を返し、セットの件数や読み上げ文言に含めない。 */
    fun currentValue(item: Gt7Ps5ManualReadoutItem): ReadoutTextEvent? {
        val data = manualValue(item)
        val value = data ?: 0
        val event =
            when (item.key) {
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value -> {
                    Gt7Ps5RemainingFuelCurrentValue(
                        value,
                        formatGt7Ps5RemainingFuelReadoutText(item.textTemplate, value),
                    )
                }

                Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value -> {
                    Gt7Ps5TyreTemperatureCurrentValue(
                        value,
                        formatGt7Ps5TyreTemperatureReadoutText(item.textTemplate, value),
                    )
                }

                Gt7Ps5ReadoutItemKey.MyBestLap.Root.value -> {
                    Gt7Ps5MyBestLap(value, formatGt7Ps5MyBestLapReadoutText(item.textTemplate, value))
                }

                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value -> {
                    val text =
                        if (value <= 0) {
                            GT7_PS5_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
                        } else {
                            formatGt7Ps5RemainingFuelLapsReadoutText(item.textTemplate, value)
                        }
                    Gt7Ps5RemainingFuelLapsCurrentValue(value, text)
                }

                else -> {
                    return null
                }
            }
        return when {
            item.textTemplate.isBlank() -> {
                event.withResolvedText("")
            }

            data == null -> {
                event.withResolvedText(manualReadoutNoDataText(gt7Ps5ManualReadoutItemName(item.key)))
            }

            else -> {
                event
            }
        }
    }
}
