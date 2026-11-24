package kurou.kodriver.domain.model

/** 「小さい音をカット」の段階値を、入力音量（0.0〜1.0）に対するカットの閾値にする。0 はカットしない（0f）。 */
public fun Int.toMicrophoneCutQuietSoundThreshold(): Float = this * MICROPHONE_CUT_QUIET_SOUND_LEVEL_STEP
