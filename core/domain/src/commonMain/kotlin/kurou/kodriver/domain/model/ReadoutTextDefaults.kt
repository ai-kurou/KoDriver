package kurou.kodriver.domain.model

/** カスタム読み上げ文言として保存できる最大文字数。 */
const val READOUT_CUSTOM_TEXT_MAX_LENGTH = 30

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

/** LMU の左車両接近開始時の読み上げ文言の初期値。空白文言の場合は読み上げない。 */
const val LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT = "カーレフト"

/** LMU の右車両接近開始時の読み上げ文言の初期値。空白文言の場合は読み上げない。 */
const val LMU_WINDOWS_VEHICLE_APPROACH_START_RIGHT_READOUT_TEXT_DEFAULT = "カーライト"

/** LMU の左車両接近継続時の読み上げ文言の初期値。空白文言の場合は読み上げない。 */
const val LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_LEFT_READOUT_TEXT_DEFAULT = "キープライト"

/** LMU の右車両接近継続時の読み上げ文言の初期値。空白文言の場合は読み上げない。 */
const val LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_RIGHT_READOUT_TEXT_DEFAULT = "キープレフト"
