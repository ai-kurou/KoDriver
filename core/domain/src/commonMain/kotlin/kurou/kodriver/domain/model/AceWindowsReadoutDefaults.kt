package kurou.kodriver.domain.model

/**
 * ACE の読み上げ有効状態。一覧画面と Narrator が共有する。
 * 新しい TopLevel は必ずここにも列挙すること（省略＝デフォルトtrue、ではない）。
 */
val ACE_WINDOWS_READOUT_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        AceWindowsReadoutItemKey.Flag.Root to true,
        AceWindowsReadoutItemKey.VehicleApproach.Root to true,
        AceWindowsReadoutItemKey.TyreTemperature.Root to true,
        AceWindowsReadoutItemKey.RemainingFuel.Root to true,
        AceWindowsReadoutItemKey.RemainingFuelLaps.Root to true,
        AceWindowsReadoutItemKey.MyBestLap.Root to false,
    )

/**
 * ACE の開始音有効状態。車両接近は頻繁に再生されるためOFF、それ以外はON。
 * 新しい TopLevel は必ずここにも列挙すること（省略＝デフォルトtrue、ではない）。
 */
val ACE_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        AceWindowsReadoutItemKey.Flag.Root to true,
        AceWindowsReadoutItemKey.VehicleApproach.Root to false,
        AceWindowsReadoutItemKey.RemainingFuel.Root to true,
        AceWindowsReadoutItemKey.RemainingFuelLaps.Root to true,
        AceWindowsReadoutItemKey.TyreTemperature.Root to true,
        AceWindowsReadoutItemKey.MyBestLap.Root to true,
    )

/**
 * ACE のキュー有効状態。supportsQueue が true の TopLevel は必ずここに列挙すること。
 * 省略＝デフォルトfalse、ではない。
 */
val ACE_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        AceWindowsReadoutItemKey.Flag.Root to false,
        AceWindowsReadoutItemKey.RemainingFuel.Root to true,
        AceWindowsReadoutItemKey.RemainingFuelLaps.Root to true,
        AceWindowsReadoutItemKey.TyreTemperature.Root to true,
        AceWindowsReadoutItemKey.MyBestLap.Root to false,
    )

/** 一覧画面の TopLevel 項目と Narrator が共有する読み上げ順序の初期値。 */
internal fun aceWindowsDefaultReadoutOrder(): List<ReadoutItemKey> =
    AceWindowsReadoutItemKey.entries
        .filterIsInstance<AceWindowsReadoutItemKey.TopLevel>()
        .sortedBy { key -> aceWindowsOrderIndex(key) }

// 新しい TopLevel を追加した際、ここで対応を判断しないとコンパイルが通らない。
private fun aceWindowsOrderIndex(key: AceWindowsReadoutItemKey.TopLevel): Int =
    when (key) {
        AceWindowsReadoutItemKey.Flag.Root -> 0
        AceWindowsReadoutItemKey.VehicleApproach.Root -> 1
        AceWindowsReadoutItemKey.RemainingFuelLaps.Root -> 2
        AceWindowsReadoutItemKey.RemainingFuel.Root -> 3
        AceWindowsReadoutItemKey.TyreTemperature.Root -> 4
        AceWindowsReadoutItemKey.MyBestLap.Root -> 5
    }
