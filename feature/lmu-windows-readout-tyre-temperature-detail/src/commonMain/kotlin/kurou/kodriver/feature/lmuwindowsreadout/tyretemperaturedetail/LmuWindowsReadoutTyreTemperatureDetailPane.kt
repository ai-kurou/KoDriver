package kurou.kodriver.feature.lmuwindowsreadout.tyretemperaturedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LmuWindowsReadoutTyreTemperatureDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel: LmuWindowsReadoutTyreTemperatureDetailViewModel = koinViewModel()
    LmuWindowsReadoutTyreTemperatureDetailPaneContent(modifier = modifier)
}

@Composable
internal fun LmuWindowsReadoutTyreTemperatureDetailPaneContent(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutTyreTemperatureDetailPanePreview() {
    LmuWindowsReadoutTyreTemperatureDetailPaneContent()
}
