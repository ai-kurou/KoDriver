package kurou.kodriver.domain.model

const val CONNECTION_CHECK_INTERVAL_MS_DEFAULT = 1_000L

/** テレメトリを最後に受信してからこの時間が経過すると「受信中」ではないとみなす閾値。 */
const val TELEMETRY_RECEIVING_TIMEOUT_MS_DEFAULT = 3_000L
