package kurou.kodriver.domain.model

/** ウェイクワードを検出してから、コマンドを待つ時間。過ぎるとウェイクワード待ちへ戻る。 */
public const val SPEECH_RECOGNITION_COMMAND_TIMEOUT_MS: Long = 6_000L

/** 読み上げが終わってから聞き取りを再開するまでの待ち時間。読み上げ直後の残響をウェイクワードと誤認しないための余裕。 */
public const val SPEECH_RECOGNITION_RESUME_DELAY_MS: Long = 500L
