package kurou.kodriver.domain.model

/**
 * WebSocket ping 間隔（ミリ秒）。
 * Wi-Fi 瞬断などで TCP がサイレントに切れた（half-open になった）接続を検知するために使う。
 * pong がこの間隔内に返らないと OkHttp が接続を失敗として扱い、
 * 各リポジトリの connectWithRetry が再接続できるようになる。
 */
const val WEB_SOCKET_PING_INTERVAL_MS = 15_000L
