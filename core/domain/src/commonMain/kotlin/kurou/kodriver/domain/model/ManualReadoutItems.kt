package kurou.kodriver.domain.model

/** シミュレーターで実装済みの手動読み上げ対象項目。 */
fun manualReadoutItemKeys(simulatorId: String): List<String> =
    when (simulatorId) {
        Simulator.LmuWindows.id -> listOf(LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value)
        else -> emptyList()
    }
