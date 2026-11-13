package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutSet

/** 統合前の「クラス内前方車とのギャップ」の項目キー。 */
private const val LEGACY_CLASS_CAR_GAP_AHEAD_KEY = "lmu_windows_auto_class_car_gap_ahead"

/** 統合前の「クラス内後方車とのギャップ」の項目キー。 */
private const val LEGACY_CLASS_CAR_GAP_BEHIND_KEY = "lmu_windows_auto_class_car_gap_behind"

private val LEGACY_CLASS_CAR_GAP_KEYS = setOf(LEGACY_CLASS_CAR_GAP_AHEAD_KEY, LEGACY_CLASS_CAR_GAP_BEHIND_KEY)

/**
 * 保存済みセットの旧「クラス内前方車／後方車とのギャップ」を、統合した [LmuWindowsReadoutItemKey.ClassCarGap.Root] へ置き換える。
 *
 * 旧キーが最初に現れた位置へ新キーを1つだけ置き、旧キーは取り除く（前方・後方の両方があっても1項目にまとめる）。
 * 旧キーの文言は秒数1つだけを置換する形式で新しい文言に変換できないため破棄し、新キーには既定文言を使う。
 * 旧キーがないセットはそのまま返す。
 */
fun migrateLmuWindowsManualReadoutLegacyClassCarGap(set: ManualReadoutSet): ManualReadoutSet {
    if (set.itemKeys.none { it in LEGACY_CLASS_CAR_GAP_KEYS } &&
        set.itemTexts.keys.none { it in LEGACY_CLASS_CAR_GAP_KEYS }
    ) {
        return set
    }
    val newKey = LmuWindowsReadoutItemKey.ClassCarGap.Root.value
    val migratedKeys = mutableListOf<String>()
    set.itemKeys.forEach { key ->
        val mapped = if (key in LEGACY_CLASS_CAR_GAP_KEYS) newKey else key
        if (mapped !in migratedKeys) migratedKeys += mapped
    }
    return set.copy(
        itemKeys = migratedKeys,
        itemTexts = set.itemTexts.filterKeys { it !in LEGACY_CLASS_CAR_GAP_KEYS },
    )
}
