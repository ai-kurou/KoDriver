package kurou.kodriver.domain.model

/**
 * 読み上げ条件が整ったイベントが、実際にどう処理されたかを表す。
 *
 * キュー再生が有効な場合は優先度判定をせずキューへ追加されるため、[SKIPPED] はキュー再生が無効で、
 * かつ再生中のイベントに優先度で負けた場合にのみ発生する。
 */
enum class NarrationOutcome(
    val id: String,
) {
    /** キュー再生が有効で、読み上げキューへ追加された。 */
    QUEUED("queued"),

    /** キュー再生が無効で優先度が勝ったため、再生中の読み上げを止めて割り込み再生した。 */
    INTERRUPTED("interrupted"),

    /** キュー再生が無効で優先度が負けたため、読み上げされなかった。 */
    SKIPPED("skipped"),
    ;

    companion object {
        fun fromId(id: String): NarrationOutcome? = entries.firstOrNull { it.id == id }
    }
}
