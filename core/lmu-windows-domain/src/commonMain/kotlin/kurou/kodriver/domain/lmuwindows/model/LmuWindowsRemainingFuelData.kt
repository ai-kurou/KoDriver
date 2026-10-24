package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable

/** 燃料の残量割合（0〜100の整数%）。燃料警告の判定とテレメトリログに使う。 */
@Serializable
data class LmuWindowsRemainingFuelData(
    val remainingPercent: Int,
)
