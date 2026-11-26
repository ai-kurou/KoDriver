package kurou.kodriver.domain.model

/** 小さい音をカットする強さの段階。0 はカットしない。段階 × [MICROPHONE_CUT_QUIET_SOUND_LEVEL_STEP] が音量ゲートのしきい値（0.0〜1.0）になる。 */
public const val MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT: Int = 3
public const val MICROPHONE_CUT_QUIET_SOUND_LEVEL_MIN: Int = 0
public const val MICROPHONE_CUT_QUIET_SOUND_LEVEL_MAX: Int = 12
public const val MICROPHONE_CUT_QUIET_SOUND_LEVEL_STEP: Float = 0.05f

/** コマンド受付中にも小さい音をカットするか。既定はカットしない（ウェイクワード検出中だけカットする）。 */
public const val MICROPHONE_CUT_QUIET_SOUND_IN_COMMAND_DEFAULT: Boolean = false
