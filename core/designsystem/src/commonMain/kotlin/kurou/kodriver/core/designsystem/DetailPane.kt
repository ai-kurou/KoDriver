package kurou.kodriver.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DetailPaneTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.height(IntrinsicSize.Min).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
fun DetailPaneDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

@Composable
fun DetailPaneSubtitle(
    text: String,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(IntrinsicSize.Min).padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.secondary),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 8.dp),
        )
        if (trailingContent != null) {
            trailingContent()
        }
    }
}
