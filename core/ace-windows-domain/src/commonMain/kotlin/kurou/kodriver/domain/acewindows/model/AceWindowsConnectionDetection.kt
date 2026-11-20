package kurou.kodriver.domain.acewindows.model

/**
 * ACE の Graphics 共有メモリで packetId が変化しなくなってから、ACE が終了・停止したとみなすまでの時間（ミリ秒）。
 */
public const val ACE_WINDOWS_PACKET_ID_STALE_THRESHOLD_MILLIS: Long = 3_000L
