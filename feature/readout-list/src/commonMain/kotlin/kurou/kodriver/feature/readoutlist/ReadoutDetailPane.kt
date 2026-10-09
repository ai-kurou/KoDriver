package kurou.kodriver.feature.readoutlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.feature.readoutlist.generated.resources.Res
import kurou.kodriver.feature.readoutlist.generated.resources.navigate_back
import kurou.kodriver.feature.readoutlist.generated.resources.root_disabled_banner_enable
import kurou.kodriver.feature.readoutlist.generated.resources.root_disabled_banner_message
import org.jetbrains.compose.resources.stringResource

private val READOUT_DETAIL_TOP_APP_BAR_HEIGHT = 56.dp
private const val READOUT_DETAIL_ROOT_DISABLED_ALPHA = 0.38f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadoutDetailPane(
    title: String,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    rootEnabled: Boolean = true,
    rootItemName: String = title,
    onEnableRoot: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
    content: @Composable () -> Unit,
) {
    val contentAlpha by animateFloatAsState(
        targetValue = if (rootEnabled) 1f else READOUT_DETAIL_ROOT_DISABLED_ALPHA,
        label = "contentAlpha",
    )
    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.navigate_back),
                            )
                        }
                    }
                },
                expandedHeight = READOUT_DETAIL_TOP_APP_BAR_HEIGHT,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            AnimatedVisibility(
                visible = !rootEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                RootDisabledBanner(itemName = rootItemName, onEnableRoot = onEnableRoot)
            }
            // 操作は可能なまま、読み上げられないことを視覚的に示すため薄く表示する。
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .alpha(contentAlpha),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun RootDisabledBanner(
    itemName: String,
    onEnableRoot: () -> Unit,
) {
    val contentColor = KoDriverExtendedColors.current.onWarningContainer
    Surface(
        onClick = onEnableRoot,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = KoDriverSpacing.large,
                    vertical = KoDriverSpacing.small,
                ).semantics { role = Role.Button },
        shape = MaterialTheme.shapes.medium,
        color = KoDriverExtendedColors.current.warningContainer,
        contentColor = contentColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.padding(KoDriverSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.WarningAmber, contentDescription = null)
            Text(
                text = stringResource(Res.string.root_disabled_banner_message, itemName),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.root_disabled_banner_enable),
                    style = MaterialTheme.typography.labelLarge,
                    textDecoration = TextDecoration.Underline,
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReadoutDetailPanePreview() {
    KoDriverTheme {
        ReadoutDetailPane(title = "フラッグ", canNavigateBack = true, onBack = {}, content = {})
    }
}
