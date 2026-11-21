package kurou.kodriver.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.generated.resources.Res
import kurou.kodriver.core.designsystem.generated.resources.navigate_back
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val DETAIL_TOP_APP_BAR_HEIGHT = 56.dp

/**
 * 詳細ペインの上部バー。戻るボタンの説明文は designsystem の文字列リソースから解決する。
 * [background] を指定すると、一覧の項目と同じ背景画像を右寄せで表示し、左端からバーの背景色へ溶け込ませる。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun DetailPaneTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIconModifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    background: ReadoutBackground? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    if (background == null) {
        DetailPaneTopAppBarContent(
            title = title,
            canNavigateBack = canNavigateBack,
            onBack = onBack,
            modifier = modifier,
            navigationIconModifier = navigationIconModifier,
            scrollBehavior = scrollBehavior,
            colors = null,
            actions = actions,
        )
        return
    }
    val barColor = MaterialTheme.colorScheme.surface
    val painter = painterResource(background.drawable)
    Box(modifier = modifier.background(barColor).rightAlignedBackgroundImage(painter, barColor)) {
        DetailPaneTopAppBarContent(
            title,
            canNavigateBack,
            onBack,
            Modifier,
            navigationIconModifier,
            scrollBehavior,
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
            ),
            actions,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailPaneTopAppBarContent(
    title: String,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier,
    navigationIconModifier: Modifier,
    scrollBehavior: TopAppBarScrollBehavior?,
    colors: TopAppBarColors?,
    actions: @Composable RowScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    TopAppBar(
        modifier = modifier,
        colors = colors ?: TopAppBarDefaults.topAppBarColors(),
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onBack()
                    },
                    modifier = navigationIconModifier,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.navigate_back),
                    )
                }
            }
        },
        actions = actions,
        expandedHeight = DETAIL_TOP_APP_BAR_HEIGHT,
        scrollBehavior = scrollBehavior,
    )
}
