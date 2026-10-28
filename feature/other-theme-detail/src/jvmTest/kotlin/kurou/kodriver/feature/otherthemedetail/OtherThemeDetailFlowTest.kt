package kurou.kodriver.feature.otherthemedetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherThemeDetailFlowTest {
    private val savedThemeMode = MutableStateFlow(ThemeMode.SYSTEM)
    private val pendingThemeMode = MutableStateFlow<ThemeMode?>(null)

    private fun createFlow() =
        OtherThemeDetailFlow(
            savedThemeMode = savedThemeMode,
            pendingThemeMode = pendingThemeMode,
        )

    @Test
    fun `保存済みテーマを保持し未保存の選択がなければ保存済みテーマを表示する`() =
        runTest {
            val flow = createFlow()
            for (saved in ThemeMode.entries) {
                for (pending in listOf(null) + ThemeMode.entries) {
                    savedThemeMode.update { saved }
                    pendingThemeMode.update { pending }
                    assertEquals(
                        OtherThemeDetailUiState(
                            selectedThemeMode = saved,
                            pendingThemeMode = pending ?: saved,
                        ),
                        flow.uiState.first(),
                    )
                }
            }
        }

    @Test
    fun `購読中の保存済みテーマ変更と選択変更と選択解除を反映する`() =
        runTest {
            val flow = createFlow()
            val states = mutableListOf<OtherThemeDetailUiState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                flow.uiState.collect { states.add(it) }
            }

            savedThemeMode.update { ThemeMode.DARK }
            pendingThemeMode.update { ThemeMode.LIGHT }
            savedThemeMode.update { ThemeMode.SYSTEM }
            pendingThemeMode.update { ThemeMode.DARK }
            pendingThemeMode.update { null }

            assertEquals(
                listOf(
                    OtherThemeDetailUiState(),
                    OtherThemeDetailUiState(ThemeMode.DARK, ThemeMode.DARK),
                    OtherThemeDetailUiState(ThemeMode.DARK, ThemeMode.LIGHT),
                    OtherThemeDetailUiState(ThemeMode.SYSTEM, ThemeMode.LIGHT),
                    OtherThemeDetailUiState(ThemeMode.SYSTEM, ThemeMode.DARK),
                    OtherThemeDetailUiState(),
                ),
                states,
            )
        }
}
