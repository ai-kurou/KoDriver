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
 *    新しいイベントのほうが大きいか等しい）: [speak] を呼ばず、読み上げない。
 * 3. キュー再生が無効で、再生中のイベントがあり、優先度が勝った: `speak(queue = false)` で割り込み再生する。
 * 4. キュー再生が無効で、再生中のイベントがない: `speak(queue = false)` でそのまま読み上げる。
 *
 * `speak(queue = false)` は再生中のイベントを止め、キューで再生待ちのイベントは残して後に再生する
 * （[NarratorEngine.speak]）。全破棄になる停止処理は、この判定からは呼ばない。
 * 優先度は再生中のイベントとだけ比較する。
 *
 * 手動読み上げ（[isManual] が true）と、手動の再生中に来た自動イベント（[currentIsManual] が true）は
 * 上の4経路とは別に扱う。手動は優先度判定をせず、再生中が自動なら割り込み、手動ならキュー末尾へ追加する。
 * 自動イベントは、キュー再生が無効で再生中が手動の場合に読み上げない。どちらも既定値（false）では従来の挙動のまま。
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
@Suppress("LongParameterList")
public fun <KEY> speakWithPriority(
    eventKey: KEY,
    currentKey: () -> KEY?,
    autoReadoutOrder: List<KEY>,
    queueEnabled: Boolean,
    speak: (queue: Boolean) -> Unit,
    isManual: Boolean = false,
    currentIsManual: () -> Boolean = { false },
): Boolean =
    speakByRoute(eventKey, currentKey, autoReadoutOrder, queueEnabled, speak, isManual, currentIsManual) !=
        SpeakRoute.SKIPPED

private enum class SpeakRoute { SKIPPED, QUEUED, INTERRUPTED, SPOKEN }

@Suppress("LongParameterList")
private fun <KEY> speakByRoute(
    eventKey: KEY,
    currentKey: () -> KEY?,
    autoReadoutOrder: List<KEY>,
    queueEnabled: Boolean,
    speak: (queue: Boolean) -> Unit,
    isManual: Boolean,
    currentIsManual: () -> Boolean,
): SpeakRoute {
    if (isManual) return speakManual(currentKey, currentIsManual, speak)
    if (queueEnabled) {
        speak(true)
        return SpeakRoute.QUEUED
    }
    val current = currentKey()
    if (current != null) {
        if (currentIsManual()) return SpeakRoute.SKIPPED
        val currentIndex = autoReadoutOrder.indexOf(current).takeIf { it != -1 } ?: Int.MAX_VALUE
        val newIndex = autoReadoutOrder.indexOf(eventKey).takeIf { it != -1 } ?: Int.MAX_VALUE
        if (newIndex >= currentIndex) return SpeakRoute.SKIPPED
    }
    speak(false)
    return if (current == null) SpeakRoute.SPOKEN else SpeakRoute.INTERRUPTED
}

/**
 * 手動読み上げ用の分岐。優先度判定をせず、再生中のイベントによって次のように動く。
 *
 * - 再生中が自動: `speak(queue = false)` で割り込み再生する。
 * - 再生中が手動: `speak(queue = true)` で末尾へ追加する。
 * - 再生中なし: `speak(queue = false)` でそのまま読み上げる。
 */
private fun <KEY> speakManual(
    currentKey: () -> KEY?,
    currentIsManual: () -> Boolean,
    speak: (queue: Boolean) -> Unit,
): SpeakRoute {
    if (currentKey() == null) {
        speak(false)
        return SpeakRoute.SPOKEN
    }
    if (currentIsManual()) {
        speak(true)
        return SpeakRoute.QUEUED
    }
    speak(false)
    return SpeakRoute.INTERRUPTED
}

/** [speakWithOutcome] が返す4経路それぞれの結果値。 */
public data class SpeakOutcomes<OUTCOME>(
    val skipped: OUTCOME,
    val queued: OUTCOME,
    val interrupted: OUTCOME,
    val spoken: OUTCOME,
)

/**
 * [speakWithPriority] の4経路を、呼び出し側が指定した [OUTCOME] の値へ変換する。
 *
 * 読み上げなしは [SpeakOutcomes.skipped]、キュー追加は [SpeakOutcomes.queued]、割り込み再生は [SpeakOutcomes.interrupted]、
 * それ以外の通常再生は [SpeakOutcomes.spoken] を返す。結果の型と値を受け取ることで `:core:domain` に依存しない。
 *
 * 経路4（再生中のイベントなし）は通常再生として扱う。
 * 経路3と取り違えると通常の読み上げが「割り込み再生」として記録される（PR #1626）。
 */
@Suppress("LongParameterList")
public fun <KEY, OUTCOME> speakWithOutcome(
    eventKey: KEY,
    currentKey: () -> KEY?,
    autoReadoutOrder: List<KEY>,
    queueEnabled: Boolean,
    speak: (queue: Boolean) -> Unit,
    outcomes: SpeakOutcomes<OUTCOME>,
    isManual: Boolean = false,
    currentIsManual: () -> Boolean = { false },
): OUTCOME =
    when (
        speakByRoute(eventKey, currentKey, autoReadoutOrder, queueEnabled, speak, isManual, currentIsManual)
    ) {
        SpeakRoute.SKIPPED -> outcomes.skipped
        SpeakRoute.QUEUED -> outcomes.queued
        SpeakRoute.INTERRUPTED -> outcomes.interrupted
        SpeakRoute.SPOKEN -> outcomes.spoken
    }
