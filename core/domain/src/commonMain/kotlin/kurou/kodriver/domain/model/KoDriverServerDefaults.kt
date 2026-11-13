package kurou.kodriver.domain.model

/** KoDriver サーバー（Ktor）およびサーバーへ接続するクライアントの既定ポート番号。 */
const val KODRIVER_SERVER_PORT_DEFAULT = 8080

/** サーバーへの接続が失敗したときに再接続を試みるまでの既定待機時間（ミリ秒）。 */
const val KODRIVER_SERVER_RETRY_DELAY_MS_DEFAULT = 3000L
