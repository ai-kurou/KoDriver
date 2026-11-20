package kurou.kodriver.domain.gt7ps5.model

/**
 * GT7 PS5 のテレメトリパケットを最後に受信してから、接続が切れたとみなすまでの時間（ミリ秒）。
 */
public const val GT7_PS5_CONNECTION_TIMEOUT_MILLIS: Long = 5_000L

/**
 * GT7 PS5 の UDP ソケットの受信タイムアウト（ミリ秒）。
 */
public const val GT7_PS5_SOCKET_TIMEOUT_MILLIS: Int = 3_000
