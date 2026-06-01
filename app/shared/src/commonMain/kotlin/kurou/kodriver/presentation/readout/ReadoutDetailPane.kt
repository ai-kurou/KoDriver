package kurou.kodriver.presentation.readout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kurou.kodriver.presentation.component.PlaceholderContent

@Composable
internal fun ReadoutDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (canNavigateBack) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
            }
        }
        PlaceholderContent(title = "detailPane", modifier = Modifier.weight(1f))
    }
}

@Preview
@Composable
fun ReadoutDetailPanePreview() {
    MaterialTheme {
        ReadoutDetailPane(canNavigateBack = true, onBack = {})
    }
}
