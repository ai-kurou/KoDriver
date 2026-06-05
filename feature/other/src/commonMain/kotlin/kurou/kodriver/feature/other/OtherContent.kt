package kurou.kodriver.feature.other

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun OtherContent(modifier: Modifier = Modifier) {
    OtherListPane(modifier = modifier)
}

@Preview
@Composable
private fun OtherContentPreview() {
    OtherContent()
}
