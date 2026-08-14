package kurou.kodriver.core.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

private val DETAIL_TOP_APP_BAR_HEIGHT = 56.dp

/**
 * DetailPaneTopAppBar を提供する公開関数。
 *
 * [hazeState] を渡すと、コンテンツ側で登録された [dev.chrisbanes.haze.hazeSource] 領域を
 * すりガラス調にぼかして背景に描画する（Haze）。省略時は通常の不透明な TopAppBar として表示する。
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun DetailPaneTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    navigateBackContentDescription: String,
    onBack: () -> Unit,
    navigationIconModifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    hazeState: HazeState? = null,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onBack, modifier = navigationIconModifier) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = navigateBackContentDescription,
                    )
                }
            }
        },
        expandedHeight = DETAIL_TOP_APP_BAR_HEIGHT,
        scrollBehavior = scrollBehavior,
        colors = if (hazeState != null) transparentTopAppBarColors() else TopAppBarDefaults.topAppBarColors(),
        modifier =
            if (hazeState != null) {
                Modifier.hazeEffect(state = hazeState, style = HazeMaterials.thin())
            } else {
                Modifier
            },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun transparentTopAppBarColors(): TopAppBarColors =
    TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
    )
