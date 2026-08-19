package kurou.kodriver.core.narrator

import kotlinx.serialization.json.Json

/**
 * テレメトリログ用の kotlinx.serialization 共通設定。
 *
 * ログフォーマットが kotlinx.serialization のデフォルト設定変更に暗黙的に追従しないよう、
 * `encodeDefaults` / `explicitNulls` を明示している。LMU / GT7 / ACE の各 narrator feature の
 * `XxxNarratorEventProcessor` が、テレメトリログ JSON をビルドする際の基底設定として利用する。
 *
 * UDP/共有メモリ由来のテレメトリに NaN/Infinity を取りうる Float/Double フィールドが含まれる場合は、
 * `Json(TelemetryLogJson) { allowSpecialFloatingPointValues = true }` のように拡張して利用する。
 */
val TelemetryLogJson: Json =
    Json {
        encodeDefaults = true
        explicitNulls = true
    }

/**
 * テレメトリログ JSON の値として埋め込めるよう、文字列を JSON 文字列リテラルへ変換する。
 *
 * `toString()` した状態オブジェクトなど、kotlinx.serialization を通さずそのままログ JSON の値として
 * 埋め込みたい文字列に対して使用する。
 */
fun String.toJsonStringLiteral(): String =
    buildString {
        append('"')
        this@toJsonStringLiteral.forEach { char ->
            when {
                char == '\\' -> append("\\\\")
                char == '"' -> append("\\\"")
                char == '\n' -> append("\\n")
                char == '\r' -> append("\\r")
                char == '\t' -> append("\\t")
                char < CONTROL_CHARACTER_BOUNDARY -> appendUnicodeEscape(char)
                else -> append(char)
            }
        }
        append('"')
    }

/**
 * [buildTelemetryLogJson] の `previous<X>` フィールド1つ分（JSON キー名と、既に JSON 文字列化された値）を
 * 表す。テレメトリの初回受信時は前回値が存在しないため、`json` は `null` を取りうる。
 */
data class TelemetryLogJsonPreviousField(
    val name: String,
    val json: String?,
)

/**
 * [buildTelemetryLogJson] の `<x>` フィールド1つ分（JSON キー名と、既に JSON 文字列化された値）を表す。
 * 現在値は必ず存在するため、[TelemetryLogJsonPreviousField] と異なり `json` は非 null。
 */
data class TelemetryLogJsonCurrentField(
    val name: String,
    val json: String,
)

/**
 * `XxxNarratorEventProcessor` がテレメトリログとして保存する JSON オブジェクトを組み立てる共通ヘルパー。
 *
 * LMU / GT7 / ACE の各 narrator feature は、判定対象のテレメトリ型やシリアライズ方法（[TelemetryLogJson] を
 * そのまま使うか `allowSpecialFloatingPointValues = true` で拡張するか等）がそれぞれ異なるため、値の
 * シリアライズ自体は呼び出し側の責務のまま、既に JSON 文字列化された値をここへ渡してもらい、
 * `state` / `previous<X>` / `<x>` / `settings` / `observedAtMs` / `finalState` という共通のキー構成へ
 * 組み立てるだけを担う。
 */
fun buildTelemetryLogJson(
    stateJson: String,
    previous: TelemetryLogJsonPreviousField,
    current: TelemetryLogJsonCurrentField,
    settingsJson: String,
    observedAtMs: Long,
    finalStateJson: String,
): String =
    "{" +
        """"state":$stateJson,""" +
        """"${previous.name}":${previous.json ?: "null"},""" +
        """"${current.name}":${current.json},""" +
        """"settings":$settingsJson,""" +
        """"observedAtMs":$observedAtMs,""" +
        """"finalState":$finalStateJson""" +
        "}"

/** JSON 仕様でエスケープが必須な制御文字の境界値 (U+0020)。これ未満の文字は全て `\uXXXX` でエスケープする。 */
private const val CONTROL_CHARACTER_BOUNDARY = ' '

private const val HEX_DIGITS = "0123456789abcdef"
private const val UNICODE_ESCAPE_HEX_DIGIT_COUNT = 4

private fun StringBuilder.appendUnicodeEscape(char: Char) {
    append("\\u")
    for (shift in (UNICODE_ESCAPE_HEX_DIGIT_COUNT - 1) downTo 0) {
        val nibble = (char.code shr (shift * 4)) and 0xF
        append(HEX_DIGITS[nibble])
    }
}
