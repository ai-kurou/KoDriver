package kurou.kodriver.feature.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.simulatorDisplayName
import kurou.kodriver.core.designsystem.simulatorIcon
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

private val SimulatorCarouselItemWidth = 160.dp
private val SimulatorCarouselItemHeight = 140.dp
private val SimulatorCarouselItemSpacing = 8.dp
private val SimulatorCarouselWidth = 320.dp

/**
 * NavigationRail / NavigationBar の先頭項目に表示する、現在選択中のシミュレータのアイコンと、
 * それをタップして開くシミュレータ選択メニュー。
 * メニューの中身は [HorizontalMultiBrowseCarousel] で表示する。
 * [simulatorId] は `kurou.kodriver.domain.model.Simulator.id` の値と一致させる必要がある。
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        ) {
            val carouselState = rememberCarouselState { Simulator.entries.size }
            HorizontalMultiBrowseCarousel(
                state = carouselState,
                preferredItemWidth = SimulatorCarouselItemWidth,
                itemSpacing = SimulatorCarouselItemSpacing,
                modifier =
                    Modifier
                        .width(SimulatorCarouselWidth)
                        .height(SimulatorCarouselItemHeight)
                        .padding(horizontal = 8.dp),
            ) { index ->
                val simulator = Simulator.entries[index]
                Column(
                    modifier =
                        Modifier
                            .maskClip(MaterialTheme.shapes.extraLarge)
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                onSimulatorSelected(simulator.id)
                                onExpandedChange(false)
                            }.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = simulatorIcon(simulator.id),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                    )
                    Text(
                        text = simulatorDisplayName(simulator.id),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
