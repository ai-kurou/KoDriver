package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.StateFlow

@Composable
expect fun <T> StateFlow<T>.collectAsStateInLifecycle(): State<T>
