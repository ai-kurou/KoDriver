package kurou.kodriver.domain.acewindows.model

import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

// detailPane（AceWindowsAutoReadoutFlagDetailViewModel）・Narrator（AceWindowsNarratorViewModel）が
// 同じデフォルト値を参照できるよう、この一箇所にのみ定義する。
internal val ACE_WINDOWS_FLAG_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        AceWindowsReadoutItemKey.Flag.WhiteFlag to true,
        AceWindowsReadoutItemKey.Flag.GreenFlag to true,
        AceWindowsReadoutItemKey.Flag.RedFlag to true,
        AceWindowsReadoutItemKey.Flag.BlueFlag to true,
        AceWindowsReadoutItemKey.Flag.YellowFlag to true,
        AceWindowsReadoutItemKey.Flag.BlackFlag to true,
        AceWindowsReadoutItemKey.Flag.BlackWhiteFlag to true,
        AceWindowsReadoutItemKey.Flag.CheckeredFlag to true,
        AceWindowsReadoutItemKey.Flag.OrangeCircleFlag to true,
        AceWindowsReadoutItemKey.Flag.RedYellowStripesFlag to true,
    )

/** ACE のチェッカーフラッグ読み上げ文言の初期値。空白文言の場合は読み上げない。 */
public const val ACE_WINDOWS_CHECKERED_FLAG_READOUT_TEXT_DEFAULT: String = "チェッカーフラッグ"

/** ACE の共通フラッグ読み上げ文言の初期値。空白文言の場合は読み上げない。 */
public const val ACE_WINDOWS_WHITE_FLAG_READOUT_TEXT_DEFAULT: String = "ホワイトフラッグ"
public const val ACE_WINDOWS_GREEN_FLAG_READOUT_TEXT_DEFAULT: String = "グリーンフラッグ"
public const val ACE_WINDOWS_RED_FLAG_READOUT_TEXT_DEFAULT: String = "レッドフラッグ"
public const val ACE_WINDOWS_BLUE_FLAG_READOUT_TEXT_DEFAULT: String = "ブルーフラッグ"
public const val ACE_WINDOWS_YELLOW_FLAG_READOUT_TEXT_DEFAULT: String = "イエローフラッグ"
public const val ACE_WINDOWS_BLACK_FLAG_READOUT_TEXT_DEFAULT: String = "ブラックフラッグ"
public const val ACE_WINDOWS_BLACK_WHITE_FLAG_READOUT_TEXT_DEFAULT: String = "ブラック・ホワイトフラッグ"

/** ACE の固有フラッグ読み上げ文言の初期値。空白文言の場合は読み上げない。 */
public const val ACE_WINDOWS_ORANGE_CIRCLE_FLAG_READOUT_TEXT_DEFAULT: String = "オレンジボールフラッグ、車両に不具合があります"
public const val ACE_WINDOWS_RED_YELLOW_STRIPES_FLAG_READOUT_TEXT_DEFAULT: String = "レッド・イエローストライプフラッグ、路面が滑りやすいです"
