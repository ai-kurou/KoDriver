package kurou.kodriver.domain.model

// listPane（ReadoutListViewModel）・Narrator（LmuWindowsNarratorViewModel / Gt7Ps5NarratorViewModel）が
// 同じデフォルト値を参照するための、シミュレーターごとのデフォルト有効状態。
// listPaneに表示される ReadoutItemKey は必ずここに列挙すること（省略＝デフォルトtrue、ではない）。
val READOUT_ENABLED_STATE_DEFAULT: Map<Simulator, Map<ReadoutItemKey, Boolean>> =
    mapOf(
        Simulator.LmuWindows to
            mapOf(
                LmuWindowsReadoutItemKey.Flag.Root to true,
                LmuWindowsReadoutItemKey.TyreTemperature.Root to true,
                LmuWindowsReadoutItemKey.VehicleApproach.Root to true,
                LmuWindowsReadoutItemKey.VehicleDamage.Root to false,
                LmuWindowsReadoutItemKey.PitTiming.Root to true,
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to false,
                LmuWindowsReadoutItemKey.TyreWear.Root to false,
                LmuWindowsReadoutItemKey.BrakeTemperature.Root to false,
                LmuWindowsReadoutItemKey.BrakeWear.Root to false,
                LmuWindowsReadoutItemKey.MyBestLap.Root to false,
            ),
        Simulator.Gt7Ps5 to
            mapOf(
                Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root to true,
                Gt7Ps5ReadoutItemKey.RemainingFuel.Root to true,
                Gt7Ps5ReadoutItemKey.TyreTemperature.Root to true,
                Gt7Ps5ReadoutItemKey.MyBestLap.Root to true,
            ),
        Simulator.AceWindows to
            mapOf(
                AceWindowsReadoutItemKey.Flag.Root to true,
                AceWindowsReadoutItemKey.VehicleApproach.Root to true,
                AceWindowsReadoutItemKey.TyreTemperature.Root to true,
                AceWindowsReadoutItemKey.RemainingFuel.Root to true,
                AceWindowsReadoutItemKey.RemainingFuelLaps.Root to true,
                AceWindowsReadoutItemKey.MyBestLap.Root to false,
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
