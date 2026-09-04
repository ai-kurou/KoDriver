package kurou.kodriver.core.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

private val DETAIL_TOP_APP_BAR_HEIGHT = 56.dp

/**
 * DetailPaneTopAppBar を提供する公開関数。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPaneTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    navigateBackContentDescription: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIconModifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val haptic = LocalHapticFeedback.current
    TopAppBar(
        modifier = modifier,
        title = { Text(title) },
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
                        contentDescription = navigateBackContentDescription,
                    )
                }
            }
        },
        expandedHeight = DETAIL_TOP_APP_BAR_HEIGHT,
        scrollBehavior = scrollBehavior,
    )
}
