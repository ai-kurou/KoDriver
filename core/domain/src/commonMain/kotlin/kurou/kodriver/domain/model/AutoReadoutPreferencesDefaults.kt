package kurou.kodriver.domain.model

// listPane（AutoReadoutListViewModel）・Narrator（LmuWindowsNarratorViewModel / Gt7Ps5NarratorViewModel）が
// 同じデフォルト値を参照するための、シミュレーターごとのデフォルト有効状態。
// 具体的な値は各シミュレーターのReadoutItemKeys specに定義する。
val READOUT_ENABLED_STATE_DEFAULT: Map<Simulator, Map<ReadoutItemKey, Boolean>>
    get() = ReadoutItemKeyRegistry.registeredSpecs.associate { it.simulator to it.autoReadoutEnabledStateDefault }

/**
 * ユーザー設定の有効状態マップから、指定した [key] の有効・無効を取得する。
 *
 * DataStore の初回読み込みが完了する前などキーが存在しない場合でも例外にならないよう、
 * [Map.getValue] の代わりに使う。TopLevel（Root）キーは [READOUT_ENABLED_STATE_DEFAULT] の値、
 * それ以外のサブ項目キーは detailPane が未保存キーに使う規約（`enabledStates[key] ?: true`）と
 * 同様にデフォルト true にフォールバックする。
 */
fun Map<ReadoutItemKey, Boolean>.readoutEnabled(key: ReadoutItemKey): Boolean =
    this[key] ?: ReadoutItemKeyRegistry.autoReadoutEnabledDefault(key) ?: true
