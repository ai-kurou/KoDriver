package kurou.kodriver.domain.model

/** Gt7Ps5 の読み上げ項目と既定値。 */
object Gt7Ps5ReadoutCatalog : SimulatorReadoutCatalog {
    override val simulator: Simulator = Simulator.Gt7Ps5
    override val entries: List<ReadoutItemKey> = Gt7Ps5ReadoutItemKey.entries
    override val enabledStateDefaults: Map<ReadoutItemKey, Boolean> = GT7_PS5_READOUT_ENABLED_STATE_DEFAULT
    override val defaultReadoutOrder: List<ReadoutItemKey> = gt7Ps5DefaultReadoutOrder()
    override val queueEnabledStateDefaults: Map<ReadoutItemKey, Boolean> = GT7_PS5_QUEUE_ENABLED_STATE_DEFAULT
    override val startSoundEnabledStateDefaults: Map<ReadoutItemKey, Boolean> =
        GT7_PS5_READOUT_START_SOUND_ENABLED_STATE_DEFAULT
}

/** シミュレーター固有の既定値で未保存の有効状態を解決する。 */
fun Map<ReadoutItemKey, Boolean>.gt7Ps5ReadoutEnabled(key: ReadoutItemKey): Boolean =
    readoutEnabled(key, GT7_PS5_READOUT_ENABLED_STATE_DEFAULT)
