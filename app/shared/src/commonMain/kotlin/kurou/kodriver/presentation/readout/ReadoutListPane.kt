package kurou.kodriver.presentation.readout

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun ReadoutListPane(onItemClick: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { LmuListItem(onClick = onItemClick) }
    }
}

@Preview
@Composable
fun ReadoutListPanePreview() {
    ReadoutListPane(onItemClick = {})
}
