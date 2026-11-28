package kurou.kodriver.domain.serverconnection.model

// `\d` は実行環境によって全角数字などにも一致するため、ASCII の範囲を明示する。
private val IPV4_OCTET_PATTERN = Regex("0|[1-9][0-9]{0,2}")

private const val IPV4_OCTET_COUNT = 4

/**
 * ドット区切り10進表記の IPv4 アドレスを4つのオクテットに変換する。形式が不正なら null を返す。
 *
 * 各オクテットは ASCII 数字のみを許可し、符号・全角数字・先頭ゼロ（`010` など）は不正とする。
 * 先頭ゼロは、解釈する側によって10進数と8進数に分かれる可能性があるため許可しない。
 */
public fun parseIpv4Octets(ip: String): List<Int>? {
    val parts = ip.split(".")
    if (parts.size != IPV4_OCTET_COUNT) return null
    val octets =
        parts.mapNotNull { part ->
            if (IPV4_OCTET_PATTERN.matches(part)) {
                part.toInt().takeIf { it <= IPV4_OCTET_MAX }
            } else {
                null
            }
        }
    return octets.takeIf { it.size == IPV4_OCTET_COUNT }
}
