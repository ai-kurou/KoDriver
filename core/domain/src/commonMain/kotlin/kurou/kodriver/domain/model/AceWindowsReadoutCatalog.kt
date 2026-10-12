package kurou.kodriver.domain.model

/** AceWindows の読み上げ項目と既定値。 */
object AceWindowsReadoutCatalog : SimulatorReadoutCatalog {
    override val simulator: Simulator = Simulator.AceWindows
    override val entries: List<ReadoutItemKey> = AceWindowsReadoutItemKey.entries
    override val enabledStateDefaults: Map<ReadoutItemKey, Boolean> = ACE_WINDOWS_READOUT_ENABLED_STATE_DEFAULT
    override val defaultReadoutOrder: List<ReadoutItemKey> = aceWindowsDefaultReadoutOrder()
    override val queueEnabledStateDefaults: Map<ReadoutItemKey, Boolean> = ACE_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT
    override val startSoundEnabledStateDefaults: Map<ReadoutItemKey, Boolean> =
        ACE_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT
}

/** シミュレーター固有の既定値で未保存の有効状態を解決する。 */
fun Map<ReadoutItemKey, Boolean>.aceWindowsReadoutEnabled(key: ReadoutItemKey): Boolean =
    readoutEnabled(key, ACE_WINDOWS_READOUT_ENABLED_STATE_DEFAULT)
