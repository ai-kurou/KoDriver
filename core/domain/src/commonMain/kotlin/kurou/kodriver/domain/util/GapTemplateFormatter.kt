package kurou.kodriver.domain.util

/**
 * ギャップ文言の節（[clause]）とプレースホルダー（[placeholder]）を置換する。
 * [seconds] が非 null のときは、プレースホルダーを秒数に置換する。
 * [seconds] が null（車なし）のときは、まず節全体を [noneClauseText] に置換し、
 * 節を含まないプレースホルダーは [noneValueText] に置換する。
 */
public fun String.replaceGap(
    clause: String,
    placeholder: String,
    seconds: String?,
    noneClauseText: String,
    noneValueText: String,
): String {
    if (seconds != null) return replace(placeholder, seconds)
    return replace(clause, noneClauseText).replace(placeholder, noneValueText)
}
