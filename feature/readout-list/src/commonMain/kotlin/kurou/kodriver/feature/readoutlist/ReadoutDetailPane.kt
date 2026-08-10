package kurou.kodriver.feature.readoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurou.kodriver.feature.readoutlist.generated.resources.Res
import kurou.kodriver.feature.readoutlist.generated.resources.navigate_back
import org.jetbrains.compose.resources.stringResource

private val READOUT_DETAIL_TOP_APP_BAR_HEIGHT = 56.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadoutDetailPane(
    title: String,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    content: @Composable () -> Unit,
) {
    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .let { scaffoldModifier ->
                    if (scrollBehavior != null) {
                        scaffoldModifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                    } else {
                        scaffoldModifier
                    }
                },
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
        Box(modifier = Modifier.padding(paddingValues)) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReadoutDetailPanePreview() {
    ReadoutDetailPane(title = "フラッグ", canNavigateBack = true, onBack = {}, content = {})
}
