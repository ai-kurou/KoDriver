package kurou.kodriver.domain.model

// readout-list（ReadoutListViewModel）が参照するデフォルト値。
// 車両接近アナウンスは接近車両ごとに頻繁に再生されるため開始音をデフォルトOFFとし、
// それ以外の ReadoutItemKey.TopLevel はデフォルトONとする。
// 新しい ReadoutItemKey.TopLevel を追加した場合は必ずここにも列挙すること（省略＝デフォルトtrue、ではない）。
val READOUT_START_SOUND_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    LMU_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT +
        GT7_PS5_READOUT_START_SOUND_ENABLED_STATE_DEFAULT +
        ACE_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT
