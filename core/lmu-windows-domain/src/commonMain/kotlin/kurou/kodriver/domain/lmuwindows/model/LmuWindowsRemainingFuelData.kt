package kurou.kodriver.domain.lmuwindows.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * 燃料の残量割合（0〜100の整数%）。燃料警告の判定とテレメトリログに使う。
 * [exactPercent] は切り捨て前の割合（0〜100）で、手動読み上げの四捨五入に使う。ログには出さない。
 */
@Serializable
data class LmuWindowsRemainingFuelData(
    val remainingPercent: Int,
    @Transient val exactPercent: Double = remainingPercent.toDouble(),
)
