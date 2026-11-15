package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow

@Composable
internal actual fun <T> StateFlow<T>.collectAsStateInLifecycle(): State<T> = collectAsState()
