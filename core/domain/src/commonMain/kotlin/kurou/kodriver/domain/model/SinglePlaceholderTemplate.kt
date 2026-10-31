package kurou.kodriver.domain.model

private val PLACEHOLDER_TOKEN_REGEX = Regex("\\{[^{}]*\\}")

/** 読み上げ文言に埋め込める `{...}` プレースホルダーを1種類だけ持つテンプレート処理。 */
@JvmInline
value class SinglePlaceholderTemplate(
    val placeholder: String,
) {
    /** [template] 内の [placeholder] をすべて [value] に置換する。 */
    fun format(
        template: String,
        value: String,
    ): String = template.replace(placeholder, value)

    /** [template] 内の `{...}` のうち、[placeholder] 以外のトークンを出現順・重複なしで返す。 */
    fun findUnknown(template: String): List<String> =
        PLACEHOLDER_TOKEN_REGEX
            .findAll(template)
            .map { it.value }
            .filter { it != placeholder }
            .distinct()
            .toList()
}
