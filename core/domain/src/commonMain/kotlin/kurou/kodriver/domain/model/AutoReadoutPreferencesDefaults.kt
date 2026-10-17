package kurou.kodriver.domain.model

import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_AUTO_READOUT_ENABLED_STATE_DEFAULT
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_AUTO_READOUT_ENABLED_STATE_DEFAULT

// listPane（AutoReadoutListViewModel）・Narrator（LmuWindowsNarratorViewModel / Gt7Ps5NarratorViewModel）が
// 同じデフォルト値を参照するための、シミュレーターごとのデフォルト有効状態。
// listPaneに表示される ReadoutItemKey は必ずここに列挙すること（省略＝デフォルトtrue、ではない）。
val READOUT_ENABLED_STATE_DEFAULT: Map<Simulator, Map<ReadoutItemKey, Boolean>> =
    mapOf(
        Simulator.LmuWindows to LMU_WINDOWS_AUTO_READOUT_ENABLED_STATE_DEFAULT,
        Simulator.Gt7Ps5 to GT7_PS5_AUTO_READOUT_ENABLED_STATE_DEFAULT,
        Simulator.AceWindows to ACE_WINDOWS_AUTO_READOUT_ENABLED_STATE_DEFAULT,
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
