package kurou.kodriver.core.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private const val DisabledContentAlpha = 0.38f

@Composable
fun DetailPaneCard(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    bottomContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailPaneCardLayout(
        title = title,
        modifier = modifier,
        titleAlpha = if (checked) 1f else DisabledContentAlpha,
        dividerAlpha = if (checked) 1f else DisabledContentAlpha,
        bottomContentAlpha = if (checked) 1f else DisabledContentAlpha,
        headerContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
            )
        },
        onHeaderClick = { onCheckedChange(!checked) },
        bottomContent = bottomContent,
    )
}

@Composable
fun DetailPaneCard(
    title: String,
    bottomContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailPaneCardLayout(
        title = title,
        modifier = modifier,
        titleAlpha = 1f,
        dividerAlpha = 1f,
        bottomContentAlpha = 1f,
        headerContent = {},
        onHeaderClick = null,
        bottomContent = bottomContent,
    )
}

@Composable
private fun DetailPaneCardLayout(
    title: String,
    titleAlpha: Float,
    dividerAlpha: Float,
    bottomContentAlpha: Float,
    headerContent: @Composable () -> Unit,
    onHeaderClick: (() -> Unit)?,
    bottomContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (onHeaderClick != null) Modifier.clickable { onHeaderClick() } else Modifier)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .alpha(titleAlpha),
                )
                headerContent()
            }
            HorizontalDivider(
                modifier = Modifier.alpha(dividerAlpha),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(bottomContentAlpha)
                    .padding(PaddingValues(horizontal = 16.dp, vertical = 12.dp)),
            ) {
                bottomContent()
            }
        }
    }
}

@Composable
fun DetailPaneCardChips(
    chipLabels: List<String>,
    selectedChipLabels: Set<String>,
    chipEnabled: Boolean,
    onChipClick: (String) -> Unit,
) {
    chipLabels.forEach { label ->
        val selected = label in selectedChipLabels
        FilterChip(
            selected = selected,
            enabled = chipEnabled,
            onClick = { onChipClick(label) },
            label = { Text(text = label) },
            leadingIcon = if (selected) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                    )
                }
            } else {
                null
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailPaneCardPreview() {
    MaterialTheme {
        Column {
            DetailPaneCard(
                title = "車両接近",
                checked = true,
                onCheckedChange = {},
                modifier = Modifier.padding(16.dp),
                bottomContent = {
                    DetailPaneCardChips(
                        chipLabels = listOf("カーレフト", "カーライト"),
                        selectedChipLabels = setOf("カーレフト"),
                        chipEnabled = true,
                        onChipClick = {},
                    )
                },
            )
            DetailPaneCard(
                title = "車両接近",
                checked = false,
                onCheckedChange = {},
                modifier = Modifier.padding(16.dp),
                bottomContent = {
                    DetailPaneCardChips(
                        chipLabels = listOf("カーレフト", "カーライト"),
                        selectedChipLabels = setOf("カーレフト"),
                        chipEnabled = false,
                        onChipClick = {},
                    )
                },
            )
            DetailPaneCard(
                title = "自己ベストラップ更新",
                modifier = Modifier.padding(16.dp),
                bottomContent = {
                    DetailPaneCardChips(
                        chipLabels = listOf("自己ベストラップ更新"),
                        selectedChipLabels = setOf("自己ベストラップ更新"),
                        chipEnabled = true,
                        onChipClick = {},
                    )
                },
            )
        }
    }
}
