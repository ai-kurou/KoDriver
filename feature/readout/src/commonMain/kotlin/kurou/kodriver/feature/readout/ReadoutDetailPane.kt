package kurou.kodriver.feature.readout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kodriver.feature.readout.generated.resources.Res
import kodriver.feature.readout.generated.resources.navigate_back
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReadoutDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (canNavigateBack) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.navigate_back))
            }
        }
        content()
    }
}

@Preview
@Composable
private fun ReadoutDetailPanePreview() {
    ReadoutDetailPane(canNavigateBack = true, onBack = {}, content = {})
}
