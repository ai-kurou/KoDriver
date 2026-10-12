package kurou.kodriver.domain.model

/** LmuWindows の読み上げ項目と既定値。 */
object LmuWindowsReadoutCatalog : SimulatorReadoutCatalog {
    override val simulator: Simulator = Simulator.LmuWindows
    override val entries: List<ReadoutItemKey> = LmuWindowsReadoutItemKey.entries
    override val enabledStateDefaults: Map<ReadoutItemKey, Boolean> = LMU_WINDOWS_READOUT_ENABLED_STATE_DEFAULT
    override val defaultReadoutOrder: List<ReadoutItemKey> = lmuWindowsDefaultReadoutOrder()
    override val queueEnabledStateDefaults: Map<ReadoutItemKey, Boolean> = LMU_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT
    override val startSoundEnabledStateDefaults: Map<ReadoutItemKey, Boolean> =
        LMU_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT
}

/** シミュレーター固有の既定値で未保存の有効状態を解決する。 */
fun Map<ReadoutItemKey, Boolean>.lmuWindowsReadoutEnabled(key: ReadoutItemKey): Boolean =
    readoutEnabled(key, LMU_WINDOWS_READOUT_ENABLED_STATE_DEFAULT)
