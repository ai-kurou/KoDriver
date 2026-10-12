package kurou.kodriver.feature.otherlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing

/**
 * オーバーレイ文字色の副テキスト。色見本と `#RRGGBB` 表記を並べる。
 */
@Composable
internal fun OtherListOverlayTextColorSummary(argb: Long) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
    ) {
        Box(
            modifier =
                Modifier
                    .size(KoDriverSpacing.large)
                    .background(Color(argb), CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
        )
        Text(argb.toRgbHexLabel())
    }
}
