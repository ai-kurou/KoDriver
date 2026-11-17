package kurou.kodriver.domain.model

/** Narrator が最新値を読み上げ判定に使ってよい受信後の最大経過時間（ミリ秒）。これを超えた値は古いとして扱う。 */
public const val NARRATOR_CURRENT_VALUE_MAX_AGE_MS: Long = 5_000L
