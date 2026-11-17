package kurou.kodriver.domain

/**
 * KoDriver サーバーを LAN 内で発見するための mDNS 定数。
 *
 * Desktop 側の広告と Android/JVM 側の探索で同じサービスタイプを使うため、
 * domain 層に置いて platform 実装から共有する。
 */
public object MdnsConstants {
    /** KoDriver サーバーが広告する DNS-SD サービスタイプ。 */
    public const val KO_DRIVER_SERVICE_TYPE: String = "_kodriver._tcp.local."
}
