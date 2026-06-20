package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DetailPaneScaffold(
    title: String,
    canNavigateBack: Boolean,
    navigateBackContentDescription: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIconModifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            DetailPaneTopAppBar(
                title = title,
                canNavigateBack = canNavigateBack,
                navigateBackContentDescription = navigateBackContentDescription,
                onBack = onBack,
                navigationIconModifier = navigationIconModifier,
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            content()
        }
    }
}
