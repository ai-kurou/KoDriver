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
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
        append('"')
    }
