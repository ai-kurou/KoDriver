package kurou.kodriver.domain.model

import kurou.kodriver.core.model.ReadoutItemKey

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
