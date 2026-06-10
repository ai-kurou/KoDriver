package kurou.kodriver.feature.readout.flag

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.readout.flag.generated.resources.Res
import kodriver.feature.readout.flag.generated.resources.flag_blue
import kodriver.feature.readout.flag.generated.resources.flag_description
import kodriver.feature.readout.flag.generated.resources.flag_full_course_yellow
import kodriver.feature.readout.flag.generated.resources.flag_red
import kodriver.feature.readout.flag.generated.resources.flag_switch_subtitle
import kodriver.feature.readout.flag.generated.resources.flag_title
import kodriver.feature.readout.flag.generated.resources.flag_yellow
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.DetailPaneTitle
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private data class FlagSwitchItem(val labelRes: StringResource, val enabled: Boolean = true)

private val flagSwitchItems = listOf(
    FlagSwitchItem(Res.string.flag_blue),
    FlagSwitchItem(Res.string.flag_yellow),
    FlagSwitchItem(Res.string.flag_full_course_yellow),
    FlagSwitchItem(Res.string.flag_red),
)

@Composable
fun FlagDetailPane(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        DetailPaneTitle(title = stringResource(Res.string.flag_title))
        DetailPaneDescription(text = stringResource(Res.string.flag_description))
        DetailPaneSubtitle(text = stringResource(Res.string.flag_switch_subtitle))
        flagSwitchItems.forEach { item ->
            FlagSwitchRow(label = stringResource(item.labelRes), checked = item.enabled)
        }
    }
}

@Composable
private fun FlagSwitchRow(
    label: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Preview(showBackground = true)
@Composable
private fun FlagDetailPanePreview() {
    FlagDetailPane()
}
