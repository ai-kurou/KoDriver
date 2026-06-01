package kurou.kodriver.presentation.readout

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun ReadoutContentPreview() {
    ReadoutContent()
}

@Preview
@Composable
fun ReadoutListPanePreview() {
    MaterialTheme {
        ReadoutListPane(onItemClick = {})
    }
}

@Preview
@Composable
fun ReadoutDetailPaneWithBackPreview() {
    MaterialTheme {
        ReadoutDetailPane(canNavigateBack = true, onBack = {})
    }
}

@Preview
@Composable
fun ReadoutDetailPaneWithoutBackPreview() {
    MaterialTheme {
        ReadoutDetailPane(canNavigateBack = false, onBack = {})
    }
}

@Preview
@Composable
fun LmuListItemPreview() {
    MaterialTheme {
        LmuListItem(onClick = {})
    }
}
