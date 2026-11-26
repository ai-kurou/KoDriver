package kurou.kodriver.feature.otherthemedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.KoDriverAlertDialog
import kurou.kodriver.core.designsystem.KoDriverDialogTextButton
import kurou.kodriver.core.designsystem.KoDriverRadioRow
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.feature.otherthemedetail.generated.resources.Res
import kurou.kodriver.feature.otherthemedetail.generated.resources.theme_cancel
import kurou.kodriver.feature.otherthemedetail.generated.resources.theme_dark
import kurou.kodriver.feature.otherthemedetail.generated.resources.theme_light
import kurou.kodriver.feature.otherthemedetail.generated.resources.theme_ok
import kurou.kodriver.feature.otherthemedetail.generated.resources.theme_system
import kurou.kodriver.feature.otherthemedetail.generated.resources.theme_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * OtherThemeDetail のダイアログを表示する Composable。
 */
@Composable
public fun OtherThemeDetailDialog(
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
    KoDriverAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.theme_title)) },
        text = {
            Column {
                Spacer(modifier = Modifier.height(KoDriverSpacing.extraSmall))
                ThemeMode.entries.forEach { themeMode ->
                    val label =
                        when (themeMode) {
                            ThemeMode.SYSTEM -> stringResource(Res.string.theme_system)
                            ThemeMode.LIGHT -> stringResource(Res.string.theme_light)
                            ThemeMode.DARK -> stringResource(Res.string.theme_dark)
                        }
                    KoDriverRadioRow(
                        selected = uiState.pendingThemeMode == themeMode,
                        label = label,
                        onClick = { onThemeModeSelected(themeMode) },
                    )
                }
            }
        },
        confirmButton = {
            KoDriverDialogTextButton(
                text = stringResource(Res.string.theme_ok),
                onClick = onConfirm,
            )
        },
        dismissButton = {
            KoDriverDialogTextButton(
                text = stringResource(Res.string.theme_cancel),
                onClick = onDismiss,
            )
        },
        modifier = modifier,
    )
}

// AlertDialog はポップアップウィンドウとして別の描画ルートで描画されるため、
// Compose Multiplatform の Res リソース配列の初期化が引き継がれずプレビューが動作しない。
@Preview(showBackground = true)
@Composable
private fun OtherThemeDetailDialogPreview() {
    KoDriverTheme {
        OtherThemeDetailDialogContent(
            uiState = OtherThemeDetailUiState(),
        )
    }
}
