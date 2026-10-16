package kurou.kodriver.core.narrator

/**
 * 優先度に基づいてイベントを読み上げるかどうかを判定する共通ロジック。
 *
 * LMU / GT7 / ACE の各 narrator feature の `XxxNarratorEventProcessor` から、`domain.engine.SpeechEvent` /
 * `domain.model.ReadoutItemKey` を型パラメータ化した形で呼び出される。`:core:narrator` 自体は `:core:domain` に
 * 依存しないため（`moduleGraphAssert` の `maxHeight` 制約を超えないため）、イベントのキー（[KEY]）は
 * 呼び出し側から値として渡してもらう。
 *
 * 処理は次の4経路に分岐する。キュー追加・通常再生・割り込み再生・読み上げなしがそれぞれ1経路ずつ対応する。
 *
 * 1. キュー再生（[queueEnabled]）が有効: 優先度判定をせず `speak(queue = true)` でキューへ追加する。
 * 2. キュー再生が無効で、再生中のイベント（[currentKey]）があり、優先度が負けた（[autoReadoutOrder] のインデックスが
 *    新しいイベントのほうが大きいか等しい）: [speak] も [stop] も呼ばず、読み上げない。
 * 3. キュー再生が無効で、再生中のイベントがあり、優先度が勝った: [stop] で現在の再生を止めてから
 *    `speak(queue = false)` で割り込み再生する。
 * 4. キュー再生が無効で、再生中のイベントがない: [stop] を呼ばず `speak(queue = false)` でそのまま読み上げる。
 *
 * [autoReadoutOrder] に含まれないキーは最も優先度が低いものとして扱う。
 * [currentKey] はキュー再生が有効な場合には呼び出し不要なため、遅延評価できるよう関数で受け取る。
 *
 * **戻り値の `Boolean` だけでは上の4経路を区別できない**（経路1・3・4がいずれも true になる）。テレメトリログへ
 * 保存する `NarrationOutcome` のように4経路を区別したい呼び出し側は、[speakWithOutcome] を利用する。
 * `:core:narrator` は `:core:domain` に依存しないため、結果の型と値は呼び出し側から渡す。
 *
 * @return 読み上げ（キュー追加・通常再生・割り込み再生のいずれか）を実行した場合 true、
 *   優先度に負けて読み上げなかった場合（経路2）は false
 */
fun <KEY> speakWithPriority(
    eventKey: KEY,
    currentKey: () -> KEY?,
    autoReadoutOrder: List<KEY>,
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
        val currentIndex = autoReadoutOrder.indexOf(current).takeIf { it != -1 } ?: Int.MAX_VALUE
        val newIndex = autoReadoutOrder.indexOf(eventKey).takeIf { it != -1 } ?: Int.MAX_VALUE
        if (newIndex >= currentIndex) return false
        stop()
    }
    speak(false)
    return true
}

/** [speakWithOutcome] が返す4経路それぞれの結果値。 */
data class SpeakOutcomes<OUTCOME>(
    val skipped: OUTCOME,
    val queued: OUTCOME,
    val interrupted: OUTCOME,
    val spoken: OUTCOME,
)

/**
 * [speakWithPriority] の4経路を、呼び出し側が指定した [OUTCOME] の値へ変換する。
 *
 * 読み上げなしは [SpeakOutcomes.skipped]、キュー追加は [SpeakOutcomes.queued]、停止を伴う割り込み再生は [SpeakOutcomes.interrupted]、
 * それ以外の通常再生は [SpeakOutcomes.spoken] を返す。結果の型と値を受け取ることで `:core:domain` に依存しない。
 *
 * 経路4（再生中のイベントなし）では [stop] が呼ばれないため、通常再生として扱う。
 * 経路3と取り違えると通常の読み上げが「割り込み再生」として記録される（PR #1626）。
 */
fun <KEY, OUTCOME> speakWithOutcome(
    eventKey: KEY,
    currentKey: () -> KEY?,
    autoReadoutOrder: List<KEY>,
    queueEnabled: Boolean,
    speak: (queue: Boolean) -> Unit,
    stop: () -> Unit,
    outcomes: SpeakOutcomes<OUTCOME>,
): OUTCOME {
    var wasQueued: Boolean? = null
    var didStop = false
    val didSpeak =
        speakWithPriority(
            eventKey = eventKey,
            currentKey = currentKey,
            autoReadoutOrder = autoReadoutOrder,
            queueEnabled = queueEnabled,
            speak = { queue ->
                wasQueued = queue
                speak(queue)
            },
            stop = {
                didStop = true
                stop()
            },
        )
    return when {
        !didSpeak -> outcomes.skipped
        wasQueued == true -> outcomes.queued
        didStop -> outcomes.interrupted
        else -> outcomes.spoken
    }
}
