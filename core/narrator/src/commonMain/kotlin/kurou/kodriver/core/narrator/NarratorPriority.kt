package kurou.kodriver.core.narrator

/**
 * 優先度に基づいてイベントを読み上げるかどうかを判定する共通ロジック。
 *
 * LMU / GT7 / ACE の各 narrator feature の `XxxNarratorEventProcessor` から、`domain.engine.SpeechEvent` /
 * `domain.model.ReadoutItemKey` を型パラメータ化した形で呼び出される。`:core:narrator` 自体は `:core:domain` に
 * 依存しないため（`moduleGraphAssert` の `maxHeight` 制約を超えないため）、イベントのキー（[KEY]）は
 * 呼び出し側から値として渡してもらう。
 *
 * キュー再生（[queueEnabled]）が有効な場合は優先度判定をせずそのままキューへ追加する。それ以外の場合は、
 * 現在再生中のイベントのキー（[currentKey]）と [readoutOrder] 上の位置を比較し、新しいイベントのほうが
 * 優先度が高い（[readoutOrder] のインデックスが小さい）場合のみ、現在の再生を [stop] してから [speak] する。
 * [readoutOrder] に含まれないキーは最も優先度が低いものとして扱う。
 *
 * [currentKey] はキュー再生が有効な場合には呼び出し不要なため、遅延評価できるよう関数で受け取る。
 *
 * @return 読み上げ（またはキュー追加）を実行した場合 true
 */
fun <KEY> speakWithPriority(
    eventKey: KEY,
    currentKey: () -> KEY?,
    readoutOrder: List<KEY>,
    queueEnabled: Boolean,
    speak: (queue: Boolean) -> Unit,
    stop: () -> Unit,
): Boolean {
    if (queueEnabled) {
        speak(true)
        return true
    }
    val current = currentKey()
    if (current != null) {
        val currentIndex = readoutOrder.indexOf(current).takeIf { it != -1 } ?: Int.MAX_VALUE
        val newIndex = readoutOrder.indexOf(eventKey).takeIf { it != -1 } ?: Int.MAX_VALUE
        if (newIndex >= currentIndex) return false
        stop()
    }
    speak(false)
    return true
}
