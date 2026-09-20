package kurou.kodriver.domain.model

/**
 * 読み上げ条件が整ったイベントが、実際にどう処理されたかを表す。
 *
 * `:core:narrator` の `speakWithPriority` が分岐する4経路と1対1に対応する。キュー再生が有効な場合は
 * 優先度判定をせずキューへ追加されるため、[SPOKEN] 以降はいずれもキュー再生が無効な場合にのみ発生する。
 */
enum class NarrationOutcome(
    val id: String,
) {
    /** キュー再生が有効で、読み上げキューへ追加された。 */
    QUEUED("queued"),

    /** キュー再生が無効で、再生中の読み上げがなかったため、そのまま読み上げた。 */
    SPOKEN("spoken"),

    /** キュー再生が無効で優先度が勝ったため、再生中の読み上げを止めて割り込み再生した。 */
    INTERRUPTED("interrupted"),

    /** キュー再生が無効で優先度が負けたため、読み上げされなかった。 */
    SKIPPED("skipped"),
    ;

    companion object {
        fun fromId(id: String): NarrationOutcome? = entries.firstOrNull { it.id == id }
    }
}
