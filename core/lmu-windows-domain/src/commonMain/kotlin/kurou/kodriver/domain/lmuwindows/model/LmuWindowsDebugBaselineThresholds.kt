package kurou.kodriver.domain.lmuwindows.model

/**
 * 燃料残量がベースラインの最小値からこの値（リットル）を超えて増えたら、補給とみなす。
 * 残量の微小な揺れを補給と誤認しないための許容値。
 */
const val LMU_WINDOWS_FUEL_REFILL_DETECTION_TOLERANCE_LITERS = 0.5

/**
 * 残量率がベースラインの最小値からこの値（比率）を超えて増えたら、交換・補給とみなす。
 * 残量の微小な揺れを補給・交換と誤認しないよう0.5%を許容する。タイヤ摩耗・仮想エネルギーで共通。
 */
const val LMU_WINDOWS_REMAINING_RATIO_INCREASE_DETECTION_TOLERANCE_RATIO = 0.005
