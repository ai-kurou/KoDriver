package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.test.assertEquals

/** 初期状態と、全設定の変更後に流れる状態を同じ型の期待値で検証する。 */
@OptIn(ExperimentalCoroutinesApi::class)
internal suspend fun <T> TestScope.assertSettingsStateUpdates(
    uiState: Flow<T>,
    initialState: T,
    updatedState: T,
    updateSettings: () -> Unit,
) {
    assertEquals(initialState, uiState.first())
    val updated =
        async(UnconfinedTestDispatcher(testScheduler)) {
            uiState.first { it == updatedState }
        }

    updateSettings()

    assertEquals(updatedState, updated.await())
}
