package kurou.kodriver.domain.model

/** 読み上げが終わってから音声認識の聞き取りを再開するまでの待ち時間(ミリ秒)。読み上げ直後の残響をウェイクワードと誤認しないための余裕。 */
const val SPEECH_RECOGNITION_RESUME_DELAY_MS_DEFAULT = 500
const val SPEECH_RECOGNITION_RESUME_DELAY_MS_MIN = 0
const val SPEECH_RECOGNITION_RESUME_DELAY_MS_MAX = 5_000
