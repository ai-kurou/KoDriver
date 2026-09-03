package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import kurou.kodriver.core.designsystem.generated.resources.Res
import kurou.kodriver.core.designsystem.generated.resources.ace
import kurou.kodriver.core.designsystem.generated.resources.ace_large
import kurou.kodriver.core.designsystem.generated.resources.gt7
import kurou.kodriver.core.designsystem.generated.resources.gt7_large
import kurou.kodriver.core.designsystem.generated.resources.lmu
import kurou.kodriver.core.designsystem.generated.resources.lmu_large
import kurou.kodriver.core.designsystem.generated.resources.simulator_name_ace_windows
import kurou.kodriver.core.designsystem.generated.resources.simulator_name_gt7_ps5
import kurou.kodriver.core.designsystem.generated.resources.simulator_name_lmu_windows
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val LMU_WINDOWS_ID = "lmu_windows"
private const val GT7_PS5_ID = "gt7_ps5"
private const val ACE_WINDOWS_ID = "ace_windows"

/**
 * [simulatorId] は `kurou.kodriver.domain.model.Simulator.id` の値と一致させる必要がある。
 * このモジュールは core:domain に依存しないため、型ではなく文字列IDを引数にとる。
 */
@Composable
fun simulatorDisplayName(simulatorId: String): String =
    when (simulatorId) {
        LMU_WINDOWS_ID -> stringResource(Res.string.simulator_name_lmu_windows)
        GT7_PS5_ID -> stringResource(Res.string.simulator_name_gt7_ps5)
        ACE_WINDOWS_ID -> stringResource(Res.string.simulator_name_ace_windows)
        else -> error("未対応のsimulatorId: $simulatorId")
    }

/** [simulatorId] は `kurou.kodriver.domain.model.Simulator.id` の値と一致させる必要がある。 */
@Composable
fun simulatorIcon(simulatorId: String): Painter =
    when (simulatorId) {
        GT7_PS5_ID -> painterResource(Res.drawable.gt7)
        LMU_WINDOWS_ID -> painterResource(Res.drawable.lmu)
        ACE_WINDOWS_ID -> painterResource(Res.drawable.ace)
        else -> error("未対応のsimulatorId: $simulatorId")
    }

/**
 * カード全体に敷き詰めるような、より大きな表示領域向けのシミュレータ画像を返す。
 * [simulatorId] は `kurou.kodriver.domain.model.Simulator.id` の値と一致させる必要がある。
 */
@Composable
fun simulatorLargeImage(simulatorId: String): Painter =
    when (simulatorId) {
        GT7_PS5_ID -> painterResource(Res.drawable.gt7_large)
        LMU_WINDOWS_ID -> painterResource(Res.drawable.lmu_large)
        ACE_WINDOWS_ID -> painterResource(Res.drawable.ace_large)
        else -> error("未対応のsimulatorId: $simulatorId")
    }

/**
 * ナビゲーション項目など表示幅が限られる場所向けの短縮名（"LMU"・"GT7"・"ACE"）を返す。
 * これらは略称であり翻訳対象ではないため文字列リソース化しない。
 * [simulatorId] は `kurou.kodriver.domain.model.Simulator.id` の値と一致させる必要がある。
 */
fun simulatorShortName(simulatorId: String): String =
    when (simulatorId) {
        LMU_WINDOWS_ID -> "LMU"
        GT7_PS5_ID -> "GT7"
        ACE_WINDOWS_ID -> "ACE"
        else -> error("未対応のsimulatorId: $simulatorId")
    }
