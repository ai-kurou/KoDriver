package kurou.kodriver.feature.readout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kodriver.feature.readout.generated.resources.Res
import kodriver.feature.readout.generated.resources.navigate_back
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReadoutDetailPane(
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (canNavigateBack) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.navigate_back))
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            Text("detailPane", fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Preview
@Composable
private fun ReadoutDetailPanePreview() {
    ReadoutDetailPane(canNavigateBack = true, onBack = {})
}
