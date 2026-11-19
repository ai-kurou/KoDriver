package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.core.designsystem.generated.resources.Res
import kurou.kodriver.core.designsystem.generated.resources.ace_pre_release_notice
import org.jetbrains.compose.resources.stringResource

/** Assetto Corsa EVO が正式リリース前であることを伝える案内。自動・手動・音声の各タブで、ACE を選択しているときだけ最上部に置く。 */
@Composable
public fun AcePreReleaseNotice(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.ace_pre_release_notice),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier.padding(bottom = KoDriverSpacing.medium),
    )
}
