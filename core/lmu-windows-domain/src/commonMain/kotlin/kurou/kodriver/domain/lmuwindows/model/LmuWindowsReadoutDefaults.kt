package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.LMU_WINDOWS_READOUT_ITEM_KEYS
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

/**
 * LMU の読み上げ有効状態。一覧画面と Narrator が共有する。
 * 新しい TopLevel は必ずここにも列挙すること（省略＝デフォルトtrue、ではない）。
 */
val LMU_WINDOWS_AUTO_READOUT_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        LmuWindowsReadoutItemKey.Flag.Root to true,
        LmuWindowsReadoutItemKey.TyreTemperature.Root to true,
        LmuWindowsReadoutItemKey.VehicleApproach.Root to true,
        LmuWindowsReadoutItemKey.VehicleDamage.Root to false,
        LmuWindowsReadoutItemKey.PitTiming.Root to true,
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to false,
        LmuWindowsReadoutItemKey.TyreWear.Root to false,
        LmuWindowsReadoutItemKey.BrakeTemperature.Root to false,
        LmuWindowsReadoutItemKey.BrakeWear.Root to false,
        LmuWindowsReadoutItemKey.MyBestLap.Root to false,
    )

/**
 * LMU の開始音有効状態。車両接近は頻繁に再生されるためOFF、それ以外はON。
 * 新しい TopLevel は必ずここにも列挙すること（省略＝デフォルトtrue、ではない）。
 */
val LMU_WINDOWS_READOUT_START_SOUND_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        LmuWindowsReadoutItemKey.VehicleApproach.Root to false,
        LmuWindowsReadoutItemKey.Flag.Root to true,
        LmuWindowsReadoutItemKey.VehicleDamage.Root to true,
        LmuWindowsReadoutItemKey.TyreTemperature.Root to true,
        LmuWindowsReadoutItemKey.PitTiming.Root to true,
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true,
        LmuWindowsReadoutItemKey.TyreWear.Root to true,
        LmuWindowsReadoutItemKey.BrakeTemperature.Root to true,
        LmuWindowsReadoutItemKey.BrakeWear.Root to true,
        LmuWindowsReadoutItemKey.MyBestLap.Root to true,
    )

/**
 * LMU のキュー有効状態。supportsQueue が true の TopLevel は必ずここに列挙すること。
 * 省略＝デフォルトfalse、ではない。
 */
val LMU_WINDOWS_QUEUE_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        LmuWindowsReadoutItemKey.Flag.Root to false,
        LmuWindowsReadoutItemKey.VehicleDamage.Root to false,
        LmuWindowsReadoutItemKey.TyreTemperature.Root to true,
        LmuWindowsReadoutItemKey.PitTiming.Root to true,
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root to true,
        LmuWindowsReadoutItemKey.TyreWear.Root to true,
        LmuWindowsReadoutItemKey.BrakeTemperature.Root to true,
        LmuWindowsReadoutItemKey.BrakeWear.Root to true,
        LmuWindowsReadoutItemKey.MyBestLap.Root to false,
    )

/** 一覧画面の TopLevel 項目と Narrator が共有する読み上げ順序の初期値。 */
internal fun lmuWindowsDefaultAutoReadoutOrder(): List<ReadoutItemKey> =
    LMU_WINDOWS_READOUT_ITEM_KEYS
        .filterIsInstance<LmuWindowsReadoutItemKey.TopLevel>()
        .sortedBy { key -> lmuWindowsOrderIndex(key) }

// 新しい TopLevel を追加した際、ここで対応を判断しないとコンパイルが通らない。
private fun lmuWindowsOrderIndex(key: LmuWindowsReadoutItemKey.TopLevel): Int =
    when (key) {
        LmuWindowsReadoutItemKey.Flag.Root -> 0
        LmuWindowsReadoutItemKey.VehicleApproach.Root -> 1
        LmuWindowsReadoutItemKey.PitTiming.Root -> 2
        LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root -> 3
        LmuWindowsReadoutItemKey.TyreTemperature.Root -> 4
        LmuWindowsReadoutItemKey.TyreWear.Root -> 5
        LmuWindowsReadoutItemKey.BrakeTemperature.Root -> 6
        LmuWindowsReadoutItemKey.BrakeWear.Root -> 7
        LmuWindowsReadoutItemKey.VehicleDamage.Root -> 8
        LmuWindowsReadoutItemKey.MyBestLap.Root -> 9
    }
