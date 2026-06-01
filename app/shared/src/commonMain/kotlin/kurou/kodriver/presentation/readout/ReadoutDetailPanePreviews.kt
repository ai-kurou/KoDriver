package kurou.kodriver.presentation.readout

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun ReadoutDetailPanePreview() {
    MaterialTheme {
        ReadoutDetailPane(canNavigateBack = true, onBack = {})
    }
}
