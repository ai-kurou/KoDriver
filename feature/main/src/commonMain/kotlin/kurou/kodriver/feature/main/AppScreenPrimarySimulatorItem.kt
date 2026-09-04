package kurou.kodriver.feature.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.simulatorDisplayName
import kurou.kodriver.core.designsystem.simulatorIcon
import kurou.kodriver.core.designsystem.simulatorLargeImage
import kurou.kodriver.core.designsystem.simulatorShortName
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.feature.main.generated.resources.Res
import kurou.kodriver.feature.main.generated.resources.select_simulator_hint
import org.jetbrains.compose.resources.stringResource

/**
 * NavigationRail / NavigationBar の先頭項目に表示する、現在選択中のシミュレータのアイコン。
 * [simulatorId] は `kurou.kodriver.domain.model.Simulator.id` の値と一致させる必要がある。
 */
@Composable
fun AppScreenPrimarySimulatorIcon(
    simulatorId: String,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = simulatorIcon(simulatorId),
        contentDescription = stringResource(Res.string.select_simulator_hint),
        modifier = modifier.clip(RoundedCornerShape(4.dp)),
    )
}

/**
 * NavigationRail / NavigationBar の先頭項目に表示する、現在選択中のシミュレータの短縮名（"LMU"・"GT7"・"ACE"）。
 * [simulatorId] は `kurou.kodriver.domain.model.Simulator.id` の値と一致させる必要がある。
 */
@Composable
fun appScreenPrimarySimulatorLabel(simulatorId: String): String = simulatorShortName(simulatorId)

private val SimulatorCardWidth = 320.dp
private val SimulatorCardHeight = 180.dp
private val SimulatorCardSpacing = 16.dp
private val SimulatorPopupMaxHeight = 480.dp
private val SimulatorCardImageBlur = 2.dp

/**
 * NavigationRail / NavigationBar の先頭項目に表示する、現在選択中のシミュレータのアイコンと、
 * それをタップして開くシミュレータ選択メニュー。
 * メニューの中身は、カード全体にシミュレータ画像を敷き詰めた等幅カードの縦スクロールで表示する。
 * [simulatorId] は `kurou.kodriver.domain.model.Simulator.id` の値と一致させる必要がある。
 */
@Composable
fun AppScreenPrimarySimulatorIndicator(
    simulatorId: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSimulatorSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box {
        AppScreenPrimarySimulatorIcon(simulatorId = simulatorId, modifier = modifier)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Column(
                modifier =
                    Modifier
                        .width(SimulatorCardWidth + SimulatorCardSpacing * 2)
                        .heightIn(max = SimulatorPopupMaxHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(SimulatorCardSpacing),
                verticalArrangement = Arrangement.spacedBy(SimulatorCardSpacing),
            ) {
                Simulator.entries.forEach { simulator ->
                    SimulatorCard(
                        simulatorId = simulator.id,
                        onClick = {
                            onSimulatorSelected(simulator.id)
                            onExpandedChange(false)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulatorCard(
    simulatorId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .width(SimulatorCardWidth)
                .height(SimulatorCardHeight)
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable(onClick = onClick),
    ) {
        Image(
            painter = simulatorLargeImage(simulatorId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(SimulatorCardImageBlur),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = simulatorDisplayName(simulatorId),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}
