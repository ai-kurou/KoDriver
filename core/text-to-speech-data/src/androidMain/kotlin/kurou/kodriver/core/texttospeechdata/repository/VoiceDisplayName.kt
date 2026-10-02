package kurou.kodriver.core.texttospeechdata.repository

/** Androidの定型音声名を日本語の表示名へ変換し、想定外の形式はそのまま返す。 */
internal fun formatVoiceDisplayName(name: String): String {
    val match = Regex("ja-jp-x-([^-]+)-([^-]+)").matchEntire(name.lowercase()) ?: return name
    val (code, variant) = match.destructured
    val variantDisplayName =
        when (variant) {
            "local" -> "ローカル"
            "network" -> "ネットワーク"
            else -> variant
        }
    return "日本語 ($code・$variantDisplayName)"
}
