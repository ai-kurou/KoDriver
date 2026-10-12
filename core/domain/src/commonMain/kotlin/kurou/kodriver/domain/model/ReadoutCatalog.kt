package kurou.kodriver.domain.model

/** 1つのシミュレーターが持つ読み上げ項目キー・既定値・既定順序。 */
interface SimulatorReadoutCatalog {
    val simulator: Simulator
    val entries: List<ReadoutItemKey>
    val enabledStateDefaults: Map<ReadoutItemKey, Boolean>
    val defaultReadoutOrder: List<ReadoutItemKey>
    val queueEnabledStateDefaults: Map<ReadoutItemKey, Boolean>
    val startSoundEnabledStateDefaults: Map<ReadoutItemKey, Boolean>
}

/** 全シミュレーターのカタログを束ねる。構成は composition root（:core:data）が決める。 */
class ReadoutCatalog(
    private val catalogs: List<SimulatorReadoutCatalog>,
) {
    val entries: List<ReadoutItemKey> = catalogs.flatMap { it.entries }
    private val enabledDefaults =
        catalogs.fold(emptyMap<ReadoutItemKey, Boolean>()) { acc, catalog ->
            acc + catalog.enabledStateDefaults
        }
    val queueEnabledStateDefaults: Map<ReadoutItemKey, Boolean> =
        catalogs.fold(emptyMap()) { acc, catalog -> acc + catalog.queueEnabledStateDefaults }
    val startSoundEnabledStateDefaults: Map<ReadoutItemKey, Boolean> =
        catalogs.fold(emptyMap()) { acc, catalog -> acc + catalog.startSoundEnabledStateDefaults }

    fun fromValue(value: String): ReadoutItemKey? = entries.find { it.value == value }

    fun enabledStateDefaults(simulator: Simulator): Map<ReadoutItemKey, Boolean> =
        catalogs.find { it.simulator == simulator }?.enabledStateDefaults.orEmpty()

    fun defaultReadoutOrder(simulator: Simulator): List<ReadoutItemKey> =
        catalogs.find { it.simulator == simulator }?.defaultReadoutOrder.orEmpty()

    fun Map<ReadoutItemKey, Boolean>.readoutEnabled(key: ReadoutItemKey): Boolean = readoutEnabled(key, enabledDefaults)
}

/** 保存値、既定値、未定義項目の true の順で有効状態を解決する。 */
fun Map<ReadoutItemKey, Boolean>.readoutEnabled(
    key: ReadoutItemKey,
    defaults: Map<ReadoutItemKey, Boolean>,
): Boolean = this[key] ?: defaults[key] ?: true
