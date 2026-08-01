package kurou.kodriver.domain

/**
 * KoDriver サーバーを LAN 内で発見するための mDNS 定数。
 *
 * Desktop 側の広告と Android/JVM 側の探索で同じサービスタイプを使うため、
 * domain 層に置いて platform 実装から共有する。
 */
object MdnsConstants {
    /** KoDriver サーバーが広告する DNS-SD サービスタイプ。 */
    const val KO_DRIVER_SERVICE_TYPE = "_kodriver._tcp.local."
}
