package kurou.kodriver.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

/**
 * [scrollToTopRequest] が正の値に変化したときに [scrollToTop] を呼び出す。
 */
@Composable
fun ScrollToTopEffect(
    scrollToTopRequest: Int,
    scrollToTop: suspend () -> Unit,
) {
    val currentScrollToTop by rememberUpdatedState(scrollToTop)
    LaunchedEffect(scrollToTopRequest) {
        if (scrollToTopRequest > 0) {
            currentScrollToTop()
        }
    }
}
