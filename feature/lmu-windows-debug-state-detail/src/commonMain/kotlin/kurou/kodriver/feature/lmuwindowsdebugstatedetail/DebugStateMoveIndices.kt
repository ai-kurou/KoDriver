package kurou.kodriver.feature.lmuwindowsdebugstatedetail

/** 固定カードを除外し、グリッドのキーを永続化対象の順序上の位置へ変換する。 */
internal fun debugStateMoveIndices(
    cardOrder: List<LmuWindowsDebugStateCardKey>,
    fromKey: Any,
    toKey: Any,
): Pair<Int, Int>? {
    val fromIndex = cardOrder.indexOfFirst { it.name == fromKey }
    val toIndex = cardOrder.indexOfFirst { it.name == toKey }
    return if (fromIndex < 0 || toIndex < 0) null else fromIndex to toIndex
}
