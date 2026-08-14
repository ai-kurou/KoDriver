package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/**
 * DetailPaneScaffold を提供する公開関数。
 *
 * TopAppBar は Haze により、背後の [content] をすりガラス調にぼかして表示する。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPaneScaffold(
    title: String,
    canNavigateBack: Boolean,
    navigateBackContentDescription: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIconModifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
    content: @Composable () -> Unit,
) {
    val hazeState = remember { HazeState() }
    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DetailPaneTopAppBar(
                title = title,
                canNavigateBack = canNavigateBack,
                navigateBackContentDescription = navigateBackContentDescription,
                onBack = onBack,
                navigationIconModifier = navigationIconModifier,
                scrollBehavior = scrollBehavior,
                hazeState = hazeState,
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).hazeSource(state = hazeState)) {
            content()
        }
    }
}
