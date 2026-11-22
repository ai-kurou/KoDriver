package kurou.kodriver.feature.autoreadoutlist

import kurou.kodriver.core.designsystem.ReadoutBackground
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutItemCategory
import kurou.kodriver.domain.model.ReadoutItemKey

/**
 * 項目のカード右側に背景として表示する画像を返す。画像を持たない項目は null。
 * タイヤ関連（温度・摩耗）はタイヤ、ブレーキ関連（温度・摩耗）はブレーキ、自己ベストラップはドライバー、
 * フラッグ（LMU・ACE）は旗の画像を使う。
 * バーチャルエネルギー残量（LMU）は仮想エネルギーの画像を使う。
 * 燃料関連（残量・残り周回数）は燃料缶の画像を使う。
 * 車両接近（LMU・ACE）は接近する車両の画像を使う。
 * ピットタイミング（LMU）はピットの画像を使う。
 * 車両故障（LMU）は故障した車両の画像を使う。
 */
internal fun autoReadoutBackground(item: ReadoutItemKey): ReadoutBackground? =
    when (item) {
        is LmuWindowsReadoutItemKey.Flag -> ReadoutBackground.FLAG
        is AceWindowsReadoutItemKey.Flag -> ReadoutBackground.FLAG
        is LmuWindowsReadoutItemKey.RemainingVirtualEnergy -> ReadoutBackground.VIRTUAL_ENERGY
        is LmuWindowsReadoutItemKey.VehicleApproach -> ReadoutBackground.VEHICLE_APPROACH
        is AceWindowsReadoutItemKey.VehicleApproach -> ReadoutBackground.VEHICLE_APPROACH
        is LmuWindowsReadoutItemKey.PitTiming -> ReadoutBackground.PIT_TIMING
        is LmuWindowsReadoutItemKey.VehicleDamage -> ReadoutBackground.VEHICLE_FAILURE
        else -> categoryBackground(item)
    }

private fun categoryBackground(item: ReadoutItemKey): ReadoutBackground? =
    when (autoReadoutItemCategory(item)) {
        ManualReadoutItemCategory.TYRE -> ReadoutBackground.TYRE
        ManualReadoutItemCategory.BRAKE -> ReadoutBackground.BRAKE
        ManualReadoutItemCategory.FUEL -> ReadoutBackground.FUEL
        ManualReadoutItemCategory.TIME -> ReadoutBackground.BEST_LAP
        else -> null
    }
