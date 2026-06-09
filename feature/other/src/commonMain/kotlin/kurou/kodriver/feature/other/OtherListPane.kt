package kurou.kodriver.feature.other

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kodriver.feature.other.generated.resources.Res
import kodriver.feature.other.generated.resources.item_license
import org.jetbrains.compose.resources.stringResource

private const val LICENSE_ITEM_ID = "license"

@Composable
private fun otherItemDisplayName(itemId: String): String = when (itemId) {
    LICENSE_ITEM_ID -> stringResource(Res.string.item_license)
    else -> itemId
}

@Composable
internal fun OtherListPane(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(listOf(LICENSE_ITEM_ID), key = { it }) { item ->
            ListItem(
                headlineContent = { Text(otherItemDisplayName(item)) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherListPanePreview() {
    OtherListPane()
}
