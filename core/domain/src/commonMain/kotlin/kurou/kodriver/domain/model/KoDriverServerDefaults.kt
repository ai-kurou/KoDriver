package kurou.kodriver.domain.model

/** KoDriver サーバー（Ktor）およびサーバーへ接続するクライアントの既定ポート番号。 */
public const val KODRIVER_SERVER_PORT_DEFAULT: Int = 8080

/** サーバーへの接続が失敗したときに再接続を試みるまでの既定待機時間（ミリ秒）。 */
public const val KODRIVER_SERVER_RETRY_DELAY_MS_DEFAULT: Long = 3000L

/** KoDriver サーバー（Ktor）の既定バインドホスト。 */
public const val KODRIVER_SERVER_HOST_DEFAULT: String = "0.0.0.0"
