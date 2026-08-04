package kurou.kodriver.domain.model

// listPane（ReadoutListViewModel）・Narrator（LmuWindowsNarratorViewModel / Gt7Ps5NarratorViewModel）が
// 同じデフォルト値を参照するための、シミュレーターごとのデフォルト有効状態。
// listPaneに表示される ReadoutItemKey は必ずここに列挙すること（省略＝デフォルトtrue、ではない）。
val READOUT_ENABLED_STATE_DEFAULT: Map<Simulator, Map<ReadoutItemKey, Boolean>> =
    mapOf(
        Simulator.LmuWindows to
            mapOf(
                ReadoutItemKey.LmuWindows.Flag.Root to true,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root to true,
                ReadoutItemKey.LmuWindows.VehicleApproach.Root to true,
                ReadoutItemKey.LmuWindows.VehicleDamage.Root to false,
                ReadoutItemKey.LmuWindows.PitTiming.Root to true,
                ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root to false,
                ReadoutItemKey.LmuWindows.TyreWear.Root to false,
                ReadoutItemKey.LmuWindows.MyBestLap.Root to false,
            ),
        Simulator.Gt7Ps5 to
            mapOf(
                ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root to true,
                ReadoutItemKey.Gt7Ps5.RemainingFuel.Root to true,
                ReadoutItemKey.Gt7Ps5.MyBestLap.Root to true,
            ),
        Simulator.AceWindows to
            mapOf(
                ReadoutItemKey.AceWindows.Flag.Root to true,
                ReadoutItemKey.AceWindows.RemainingFuel.Root to true,
            ),
    )

// [READOUT_ENABLED_STATE_DEFAULT] を ReadoutItemKey 単位にフラット化したもの。
// TopLevel（Root）キーはここに定義したデフォルト値を使う。
private val READOUT_ITEM_KEY_ENABLED_DEFAULT: Map<ReadoutItemKey, Boolean> =
    READOUT_ENABLED_STATE_DEFAULT.values.fold(emptyMap()) { acc, map -> acc + map }

/**
 * ユーザー設定の有効状態マップから、指定した [key] の有効・無効を取得する。
 *
 * DataStore の初回読み込みが完了する前などキーが存在しない場合でも例外にならないよう、
 * [Map.getValue] の代わりに使う。TopLevel（Root）キーは [READOUT_ENABLED_STATE_DEFAULT] の値、
 * それ以外のサブ項目キーは detailPane が未保存キーに使う規約（`enabledStates[key] ?: true`）と
 * 同様にデフォルト true にフォールバックする。
 */
fun Map<ReadoutItemKey, Boolean>.readoutEnabled(key: ReadoutItemKey): Boolean =
    this[key] ?: READOUT_ITEM_KEY_ENABLED_DEFAULT[key] ?: true
