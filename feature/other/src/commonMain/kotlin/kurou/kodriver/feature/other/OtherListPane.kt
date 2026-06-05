package kurou.kodriver.feature.other

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun OtherListPane(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
    }
}

@Preview
@Composable
private fun OtherListPanePreview() {
    OtherListPane()
}
