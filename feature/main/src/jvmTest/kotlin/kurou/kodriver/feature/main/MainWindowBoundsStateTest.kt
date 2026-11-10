package kurou.kodriver.feature.main

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.captureError
import kurou.kodriver.domain.model.MainWindowBounds
import kurou.kodriver.domain.repository.MainWindowBoundsPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveMainWindowBoundsUseCase
import kurou.kodriver.domain.usecase.SaveMainWindowBoundsUseCase
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test

private const val LOADING_LABEL = "読み込み中"
private const val POSITION_UNSPECIFIED_LABEL = "未設定"

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class MainWindowBoundsStateTest {
    private class FakeMainWindowBoundsPreferencesRepository(
        bounds: MainWindowBounds,
    ) : MainWindowBoundsPreferencesRepository {
        private val state = MutableStateFlow(bounds)

        override fun observeMainWindowBounds(): Flow<MainWindowBounds> = state

        override suspend fun saveMainWindowBounds(bounds: MainWindowBounds) {
            state.value = bounds
        }
    }

    /**
     * DataStore の初回読み出しのように、最初の値を emit しないまま待機し続ける Repository。
     */
    private class NeverEmittingMainWindowBoundsPreferencesRepository : MainWindowBoundsPreferencesRepository {
        override fun observeMainWindowBounds(): Flow<MainWindowBounds> = flow { }

        override suspend fun saveMainWindowBounds(bounds: MainWindowBounds) = Unit
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `保存された位置・サイズを返す`() {
        startKoinWith(
            FakeMainWindowBoundsPreferencesRepository(
                MainWindowBounds(x = 10, y = 20, width = 300, height = 100),
            ),
        )

        runComposeUiTest {
            setContent { Text(rememberBoundsLabel()) }

            onNodeWithText("10,20,300,100").assertIsDisplayed()
        }
    }

    @Test
    fun `位置が未保存の場合はxとyをnullで渡す`() {
        startKoinWith(FakeMainWindowBoundsPreferencesRepository(MainWindowBounds()))

        runComposeUiTest {
            setContent { Text(rememberBoundsLabel()) }

            onNodeWithText("未設定,未設定,1000,600").assertIsDisplayed()
        }
    }

    @Test
    fun `読み込みが完了するまではnullを返す`() {
        startKoinWith(NeverEmittingMainWindowBoundsPreferencesRepository())

        runComposeUiTest {
            setContent { Text(rememberBoundsLabel()) }

            onNodeWithText(LOADING_LABEL).assertIsDisplayed()
        }
    }

    @Test
    fun `saverで保存した位置・サイズが購読側へ反映される`() {
        startKoinWith(FakeMainWindowBoundsPreferencesRepository(MainWindowBounds()))

        runComposeUiTest {
            setContent {
                val save = rememberMainWindowBoundsSaver()
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

    @Test
    fun `保存が失敗しても記録して例外を伝播させない`() =
        runTest {
            val error = IllegalStateException("save failed")
            val repository =
                object : MainWindowBoundsPreferencesRepository {
                    override fun observeMainWindowBounds(): Flow<MainWindowBounds> = flow { }

                    override suspend fun saveMainWindowBounds(bounds: MainWindowBounds): Unit = throw error
                }
            val errorCapture = Class.forName("kurou.kodriver.domain.ErrorCaptureKt").kotlin
            mockkStatic(errorCapture)
            every { captureError(error) } returns Unit
            try {
                val save = createMainWindowBoundsSaver(SaveMainWindowBoundsUseCase(repository), this)

                save(1, 2, 400, 150)
                advanceUntilIdle()

                verify(exactly = 1) { captureError(error) }
                confirmVerified(errorCapture)
            } finally {
                unmockkStatic(errorCapture)
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
        rememberMainWindowBounds { x, y, width, height ->
            "${x.toPositionLabel()},${y.toPositionLabel()},$width,$height"
        } ?: LOADING_LABEL

    private fun Int?.toPositionLabel(): String = this?.toString() ?: POSITION_UNSPECIFIED_LABEL

    private fun startKoinWith(repository: MainWindowBoundsPreferencesRepository) {
        startKoin {
            modules(
                module {
                    factory { ObserveMainWindowBoundsUseCase(repository) }
                    factory { SaveMainWindowBoundsUseCase(repository) }
                },
            )
        }
    }
}
