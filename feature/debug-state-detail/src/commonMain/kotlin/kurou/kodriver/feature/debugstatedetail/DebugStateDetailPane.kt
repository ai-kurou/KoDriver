package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kodriver.feature.debugstatedetail.generated.resources.Res
import kodriver.feature.debugstatedetail.generated.resources.debug_state_title
import kodriver.feature.debugstatedetail.generated.resources.navigate_back
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import org.jetbrains.compose.resources.stringResource

private val NARROW_WIDTH_UPPER_BOUND = 400.dp
private val MEDIUM_WIDTH_UPPER_BOUND = 700.dp

internal fun calculateDebugStateColumns(maxWidth: Dp): Int = when {
    maxWidth < NARROW_WIDTH_UPPER_BOUND -> 1
    maxWidth < MEDIUM_WIDTH_UPPER_BOUND -> 2
    else -> 3
}

@Composable
fun DebugStateDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailPaneScaffold(
        title = stringResource(Res.string.debug_state_title),
        canNavigateBack = canNavigateBack,
        navigateBackContentDescription = stringResource(Res.string.navigate_back),
        onBack = onBack,
        modifier = modifier,
    ) {
        BoxWithConstraints {
            val columns = calculateDebugStateColumns(maxWidth)
            LazyVerticalGrid(columns = GridCells.Fixed(columns)) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DebugStateDetailPanePreview() {
    DebugStateDetailPane(
        canNavigateBack = true,
        onBack = {},
    )
}
