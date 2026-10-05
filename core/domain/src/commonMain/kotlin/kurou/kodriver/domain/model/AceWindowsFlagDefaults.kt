package kurou.kodriver.domain.model

// detailPane（AceWindowsReadoutFlagDetailViewModel）・Narrator（AceWindowsNarratorViewModel）が
// 同じデフォルト値を参照できるよう、この一箇所にのみ定義する。
val ACE_WINDOWS_FLAG_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        ReadoutItemKey.AceWindows.Flag.WhiteFlag to true,
        ReadoutItemKey.AceWindows.Flag.GreenFlag to true,
        ReadoutItemKey.AceWindows.Flag.RedFlag to true,
        ReadoutItemKey.AceWindows.Flag.BlueFlag to true,
        ReadoutItemKey.AceWindows.Flag.YellowFlag to true,
        ReadoutItemKey.AceWindows.Flag.BlackFlag to true,
        ReadoutItemKey.AceWindows.Flag.BlackWhiteFlag to true,
        ReadoutItemKey.AceWindows.Flag.CheckeredFlag to true,
        ReadoutItemKey.AceWindows.Flag.OrangeCircleFlag to true,
        ReadoutItemKey.AceWindows.Flag.RedYellowStripesFlag to true,
    )

/** ACE のチェッカーフラッグ読み上げ文言の初期値。空白文言の場合は読み上げない。 */
const val ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT = "チェッカーフラッグ"

/** ACE の共通フラッグ読み上げ文言の初期値。空白文言の場合は読み上げない。 */
const val ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT = "ホワイトフラッグ"
const val ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT = "グリーンフラッグ"
const val ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT = "レッドフラッグ"
const val ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT = "ブルーフラッグ"
const val ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT = "イエローフラッグ"
const val ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT = "ブラックフラッグ"
const val ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT = "ブラック・ホワイトフラッグ"

/** ACE の固有フラッグ読み上げ文言の初期値。空白文言の場合は読み上げない。 */
const val ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT = "オレンジボールフラッグ、車両に不具合があります"
const val ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT = "レッド・イエローストライプフラッグ、路面が滑りやすいです"
