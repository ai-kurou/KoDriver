package kurou.kodriver.feature.otherlist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ScrollToTopEffect

/**
 * OtherList の画面を表示する Composable。
 */
@Composable
fun OtherListPane(
    uiState: OtherListUiState,
    onItemClick: (OtherListItemType) -> Unit,
    onOverlayVisibleChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onDynamicColorEnabledChange: (Boolean) -> Unit,
    onHapticFeedbackEnabledChange: (Boolean) -> Unit,
    onStartupEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onAppVersionTapped: () -> Unit = {},
    scrollToTopRequest: Int = 0,
) {
    val listState = rememberLazyListState()
    val groupedItems = remember(uiState.items) { uiState.items.groupBy { it.section() } }

    ScrollToTopEffect(scrollToTopRequest = scrollToTopRequest) {
        listState.animateScrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier =
            modifier
                .fillMaxSize()
                .padding(vertical = KoDriverSpacing.small),
    ) {
        otherListSections.forEach { section ->
            val sectionItems = groupedItems[section].orEmpty()
            if (sectionItems.isNotEmpty()) {
                item(key = "section_${section.name}") {
                    OtherListSectionHeader(section)
                    HorizontalDivider()
                }
                items(sectionItems, key = { it.id }) { item ->
                    OtherListItem(
                        item = item,
                        uiState = uiState,
                        onOverlayVisibleChange = onOverlayVisibleChange,
                        onKeepScreenOnChange = onKeepScreenOnChange,
                        onDynamicColorEnabledChange = onDynamicColorEnabledChange,
                        onHapticFeedbackEnabledChange = onHapticFeedbackEnabledChange,
                        onStartupEnabledChange = onStartupEnabledChange,
                        onItemClick = onItemClick,
                    )
                    HorizontalDivider()
                }
            }
        }
        if (uiState.appVersionLabel.isNotBlank() && uiState.appVersion.isNotBlank()) {
            item(key = "app_version") {
                OtherAppVersionListItem(
                    appVersionLabel = uiState.appVersionLabel,
                    appVersion = uiState.appVersion,
                    onAppVersionTapped = onAppVersionTapped,
                )
                HorizontalDivider()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OtherListPanePreview() {
    KoDriverTheme {
        OtherListPane(
            uiState = OtherListUiState(),
            onItemClick = {},
            onOverlayVisibleChange = {},
            onKeepScreenOnChange = {},
            onDynamicColorEnabledChange = {},
            onHapticFeedbackEnabledChange = {},
            onStartupEnabledChange = {},
        )
    }
}
