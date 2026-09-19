package kurou.kodriver.feature.narratoroverlay

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kurou.kodriver.domain.model.OverlayWindowBounds
import kurou.kodriver.domain.repository.OverlayWindowBoundsPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveOverlayWindowBoundsUseCase
import kurou.kodriver.domain.usecase.SaveOverlayWindowBoundsUseCase
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test

private const val LOADING_LABEL = "読み込み中"
private const val POSITION_UNSPECIFIED_LABEL = "未設定"

@OptIn(ExperimentalTestApi::class)
class NarratorOverlayBoundsTest {
    private class FakeOverlayWindowBoundsPreferencesRepository(
        bounds: OverlayWindowBounds,
    ) : OverlayWindowBoundsPreferencesRepository {
        private val state = MutableStateFlow(bounds)

        override fun observeOverlayWindowBounds(): Flow<OverlayWindowBounds> = state

        override suspend fun saveOverlayWindowBounds(bounds: OverlayWindowBounds) {
            state.value = bounds
        }
    }

    /**
     * DataStore の初回読み出しのように、最初の値を emit しないまま待機し続ける Repository。
     */
    private class NeverEmittingOverlayWindowBoundsPreferencesRepository : OverlayWindowBoundsPreferencesRepository {
        override fun observeOverlayWindowBounds(): Flow<OverlayWindowBounds> = flow { }

        override suspend fun saveOverlayWindowBounds(bounds: OverlayWindowBounds) = Unit
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `保存された位置・サイズを返す`() {
        startKoinWith(
            FakeOverlayWindowBoundsPreferencesRepository(
                OverlayWindowBounds(x = 10, y = 20, width = 300, height = 100),
            ),
        )

        runComposeUiTest {
            setContent { Text(rememberBoundsLabel()) }

            onNodeWithText("10,20,300,100").assertIsDisplayed()
        }
    }

    @Test
    fun `位置が未保存の場合はxとyをnullで渡す`() {
        startKoinWith(FakeOverlayWindowBoundsPreferencesRepository(OverlayWindowBounds()))

        runComposeUiTest {
            setContent { Text(rememberBoundsLabel()) }

            onNodeWithText("未設定,未設定,480,120").assertIsDisplayed()
        }
    }

    @Test
    fun `読み込みが完了するまではnullを返す`() {
        startKoinWith(NeverEmittingOverlayWindowBoundsPreferencesRepository())

        runComposeUiTest {
            setContent { Text(rememberBoundsLabel()) }

            onNodeWithText(LOADING_LABEL).assertIsDisplayed()
        }
    }

    @Test
    fun `saverで保存した位置・サイズが購読側へ反映される`() {
        startKoinWith(FakeOverlayWindowBoundsPreferencesRepository(OverlayWindowBounds()))

        runComposeUiTest {
            setContent {
                val save = rememberNarratorOverlayBoundsSaver()
                val label = rememberBoundsLabel()
                Text(label)
                if (label != LOADING_LABEL && label != "1,2,400,150") {
                    save(1, 2, 400, 150)
                }
            }
            waitForIdle()

            onNodeWithText("1,2,400,150").assertIsDisplayed()
        }
    }

    /**
     * 位置が未保存の場合は `x` / `y` が null で渡されることもあわせて確認するためのラベル。
     *
     * 未保存を表す null は、文字列補間がそのまま `"null"` を埋め込むのを避けるため
     * [POSITION_UNSPECIFIED_LABEL] に置き換えて表示する。
     */
    @Composable
    private fun rememberBoundsLabel(): String =
        rememberNarratorOverlayBounds { x, y, width, height ->
            "${x.toPositionLabel()},${y.toPositionLabel()},$width,$height"
        } ?: LOADING_LABEL

    private fun Int?.toPositionLabel(): String = this?.toString() ?: POSITION_UNSPECIFIED_LABEL

    private fun startKoinWith(repository: OverlayWindowBoundsPreferencesRepository) {
        startKoin {
            modules(
                module {
                    factory { ObserveOverlayWindowBoundsUseCase(repository) }
                    factory { SaveOverlayWindowBoundsUseCase(repository) }
                },
            )
        }
    }
}
