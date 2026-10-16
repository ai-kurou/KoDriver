package kurou.kodriver.domain.model

/** シミュレーターで実装済みの手動読み上げ対象項目。 */
fun manualReadoutItemKeys(simulatorId: String): List<String> =
    when (simulatorId) {
        Simulator.LmuWindows.id -> {
            listOf(
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                LmuWindowsReadoutItemKey.TyreWear.Root.value,
            )
        }

        else -> {
            emptyList()
        }
    }

/** 手動読み上げ項目の既定文言。未対応の項目は空文字を返す。 */
fun manualReadoutItemTextDefault(itemKey: String): String =
    when (itemKey) {
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value -> {
            LMU_WINDOWS_MANUAL_READOUT_REMAINING_VIRTUAL_ENERGY_TEXT_DEFAULT
        }

        LmuWindowsReadoutItemKey.TyreWear.Root.value -> {
            LMU_WINDOWS_MANUAL_READOUT_TYRE_WEAR_TEXT_DEFAULT
        }

        else -> {
            ""
        }
    }
