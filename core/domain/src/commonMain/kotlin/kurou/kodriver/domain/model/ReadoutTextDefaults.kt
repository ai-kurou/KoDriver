package kurou.kodriver.domain.model

/**
 * カスタム読み上げ文言の未設定状態を表す値。
 *
 * 空文字の場合は収録済みWAVで読み上げ、文言が設定されている場合はOS標準のTTSで読み上げる。
 */
const val READOUT_CUSTOM_TEXT_DEFAULT = ""

/** カスタム読み上げ文言として保存できる最大文字数。 */
const val READOUT_CUSTOM_TEXT_MAX_LENGTH = 30

/**
 * フラッグ読み上げで「収録音声を使う」が明示的に選ばれているかの初期値。
 *
 * false の場合は従来どおり、カスタム文言が設定されていればそれを、未設定なら収録音声を使う。
 * true の場合はカスタム文言が残っていても収録音声で読み上げる。
 */
const val READOUT_RECORDED_VOICE_SELECTED_DEFAULT = false

/**
 * LMU のブルーフラッグ読み上げ文言の初期値。
 *
 * ブルーフラッグは収録音声のチップを持たず、自由文字列の読み上げのみを提供する。
 * 空文字にした場合のみ、収録済みWAVで読み上げる。
 */
const val LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT = "ブルーフラッグ"

/** LMU のイエローフラッグ読み上げ文言の初期値。空欄の場合は収録済みWAVにフォールバックする。 */
const val LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT = "イエローフラッグ"

/** LMU のフルコースイエロー読み上げ文言の初期値。空欄の場合は収録済みWAVにフォールバックする。 */
const val LMU_WINDOWS_FULL_COURSE_YELLOW_FLAG_READOUT_TEXT_DEFAULT = "フルコースイエロー"

/** LMU のレッドフラッグ読み上げ文言の初期値。空欄の場合は収録済みWAVにフォールバックする。 */
const val LMU_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT = "レッドフラッグ"
