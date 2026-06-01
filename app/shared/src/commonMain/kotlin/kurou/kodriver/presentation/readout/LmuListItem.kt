package kurou.kodriver.presentation.readout

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.lmu
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun LmuListItem(onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text("Le Mans Ultimate") },
        leadingContent = {
            Image(
                painter = painterResource(Res.drawable.lmu),
                contentDescription = "Le Mans Ultimate",
                modifier = Modifier.size(40.dp),
            )
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Preview
@Composable
fun LmuListItemPreview() {
    LmuListItem(onClick = {})
}

