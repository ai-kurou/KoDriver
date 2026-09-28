package kurou.kodriver.domain.model

/**
 * カスタム読み上げ文言の未設定状態を表す値。
 *
 * 空文字の場合は収録済みWAVで読み上げ、文言が設定されている場合はOS標準のTTSで読み上げる。
 */
const val READOUT_CUSTOM_TEXT_DEFAULT = ""

/** カスタム読み上げ文言として保存できる最大文字数。 */
const val READOUT_CUSTOM_TEXT_MAX_LENGTH = 30
