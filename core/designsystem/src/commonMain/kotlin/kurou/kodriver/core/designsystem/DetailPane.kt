package kurou.kodriver.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * DetailPaneBodyText を提供する公開関数。
 */
@Composable
fun DetailPaneBodyText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(vertical = 4.dp),
    )
}

/**
 * DetailPaneDescription を提供する公開関数。
 */
@Composable
fun DetailPaneDescription(
    text: String,
    modifier: Modifier = Modifier,
) {
    DetailPaneBodyText(
        text = text,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

/**
 * DetailPaneSubtitle を提供する公開関数。
 */
@Composable
fun DetailPaneSubtitle(
    text: String,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(64.dp).padding(vertical = 8.dp),
    ) {
        Box(
            modifier =
                Modifier
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

/**
 * ヘルプアイコンをタップすると、[sheetContent] を内容とする ModalBottomSheet を表示するアイコンボタン。
 * 開閉状態は内部で保持する。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpIconButton(
    contentDescription: String,
    modifier: Modifier = Modifier,
    sheetContent: @Composable () -> Unit,
) {
    var showHelpSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    IconButton(onClick = { showHelpSheet = true }, modifier = modifier) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.secondary,
        )
    }

    if (showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            sheetState = sheetState,
        ) {
            sheetContent()
        }
    }
}
