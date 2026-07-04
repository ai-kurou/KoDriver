package kurou.kodriver.feature.otherthemedetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.otherthemedetail.generated.resources.Res
import kodriver.feature.otherthemedetail.generated.resources.theme_cancel
import kodriver.feature.otherthemedetail.generated.resources.theme_dark
import kodriver.feature.otherthemedetail.generated.resources.theme_light
import kodriver.feature.otherthemedetail.generated.resources.theme_ok
import kodriver.feature.otherthemedetail.generated.resources.theme_system
import kodriver.feature.otherthemedetail.generated.resources.theme_title
import kurou.kodriver.domain.model.ThemeMode
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OtherThemeDetailDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherThemeDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherThemeDetailDialogContent(
        uiState = uiState,
        onThemeModeSelected = viewModel::onPendingThemeModeSelected,
        onConfirm = {
            viewModel.onConfirm()
            onDismiss()
        },
        onDismiss = {
            viewModel.onDismiss()
            onDismiss()
        },
        modifier = modifier,
    )
}

@Composable
internal fun OtherThemeDetailDialogContent(
    uiState: OtherThemeDetailUiState,
    onThemeModeSelected: (ThemeMode) -> Unit = {},
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.theme_title)) },
        text = {
            Column {
                Spacer(modifier = Modifier.height(4.dp))
                ThemeMode.entries.forEach { themeMode ->
                    val label = when (themeMode) {
                        ThemeMode.SYSTEM -> stringResource(Res.string.theme_system)
                        ThemeMode.LIGHT -> stringResource(Res.string.theme_light)
                        ThemeMode.DARK -> stringResource(Res.string.theme_dark)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeModeSelected(themeMode) },
                    ) {
                        RadioButton(
                            selected = uiState.pendingThemeMode == themeMode,
                            onClick = { onThemeModeSelected(themeMode) },
                        )
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.theme_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.theme_cancel))
            }
        },
        modifier = modifier,
    )
}

// AlertDialog はポップアップウィンドウとして別の描画ルートで描画されるため、
// Compose Multiplatform の Res リソース配列の初期化が引き継がれずプレビューが動作しない。
@Preview(showBackground = true)
@Composable
private fun OtherThemeDetailDialogPreview() {
    OtherThemeDetailDialogContent(
        uiState = OtherThemeDetailUiState(),
    )
}
