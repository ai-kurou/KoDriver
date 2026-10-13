package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.feature.manualreadoutlist.generated.resources.Res
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_empty_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ManualReadoutListPane(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(KoDriverSpacing.extraLarge),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.manual_readout_list_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}
