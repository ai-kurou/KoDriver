package kurou.kodriver.domain.lmuwindows.model

/**
 * LMU のブルーフラッグ読み上げ文言の初期値。
 *
 * ブルーフラッグは収録音声のチップを持たず、自由文字列の読み上げのみを提供する。
 * 空白文言の場合は読み上げない。
 */
const val LMU_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT = "ブルーフラッグ"

/** LMU のイエローフラッグ読み上げ文言の初期値。空白文言の場合は読み上げない。 */
const val LMU_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT = "イエローフラッグ"

/** LMU のフルコースイエロー読み上げ文言の初期値。空白文言の場合は読み上げない。 */
const val LMU_WINDOWS_FULL_COURSE_YELLOW_FLAG_READOUT_TEXT_DEFAULT = "フルコースイエロー"

/** LMU のレッドフラッグ読み上げ文言の初期値。空白文言の場合は読み上げない。 */
const val LMU_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT = "レッドフラッグ"
