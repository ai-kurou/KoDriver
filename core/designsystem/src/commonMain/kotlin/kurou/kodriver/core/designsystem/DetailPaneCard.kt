package kurou.kodriver.core.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
    chipLabels: List<String>,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    selectedChipLabels: Set<String> = emptySet(),
    onChipClick: (String) -> Unit = {},
) {
    DetailPaneCardLayout(
        title = title,
        chipLabels = chipLabels,
        selectedChipLabels = selectedChipLabels,
        chipEnabled = checked,
        onChipClick = onChipClick,
        modifier = modifier,
        titleAlpha = if (checked) 1f else DisabledContentAlpha,
        dividerAlpha = if (checked) 1f else DisabledContentAlpha,
        chipRowAlpha = if (checked) 1f else DisabledContentAlpha,
        headerContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
            )
        },
        onHeaderClick = { onCheckedChange(!checked) },
    )
}

@Composable
fun DetailPaneCard(
    title: String,
    chipLabels: List<String>,
    modifier: Modifier = Modifier,
    selectedChipLabels: Set<String> = emptySet(),
    onChipClick: (String) -> Unit = {},
) {
    DetailPaneCardLayout(
        title = title,
        chipLabels = chipLabels,
        selectedChipLabels = selectedChipLabels,
        chipEnabled = true,
        onChipClick = onChipClick,
        modifier = modifier,
        titleAlpha = 1f,
        dividerAlpha = 1f,
        chipRowAlpha = 1f,
        headerContent = {},
        onHeaderClick = null,
    )
}

@Composable
private fun DetailPaneCardLayout(
    title: String,
    chipLabels: List<String>,
    selectedChipLabels: Set<String>,
    chipEnabled: Boolean,
    onChipClick: (String) -> Unit,
    titleAlpha: Float,
    dividerAlpha: Float,
    chipRowAlpha: Float,
    headerContent: @Composable () -> Unit,
    onHeaderClick: (() -> Unit)?,
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
                    .alpha(chipRowAlpha)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
        }
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
                chipLabels = listOf("カーレフト", "カーライト"),
                selectedChipLabels = setOf("カーレフト"),
                onCheckedChange = {},
                modifier = Modifier.padding(16.dp),
            )
            DetailPaneCard(
                title = "車両接近",
                checked = false,
                chipLabels = listOf("カーレフト", "カーライト"),
                selectedChipLabels = setOf("カーレフト"),
                onCheckedChange = {},
                modifier = Modifier.padding(16.dp),
            )
            DetailPaneCard(
                title = "自己ベストラップ更新",
                chipLabels = emptyList(),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
