package kurou.kodriver.feature.otherthemedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherThemePreviewWiringTest {
    private val rule = createComposeRule()

    @get:Rule
    val rules: TestRule =
        RuleChain
            .outerRule(
                object : ExternalResource() {
                    override fun before() {
                        Dispatchers.setMain(UnconfinedTestDispatcher())
                    }

                    override fun after() {
                        Dispatchers.resetMain()
                    }
                },
            ).around(rule)

    private val repository = FakeThemePreferencesRepository()

    private var showDialog by mutableStateOf(true)
    private var darkTheme = false
    private var backgroundColor = Color.Unspecified

    @Before
    fun setUp() {
        startKoin {
            modules(
                module { single<ThemePreferencesRepository> { repository } },
                otherThemeDetailModule,
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `選択で背景をプレビューしキャンセルで復元して再表示時も保存済み値を使う`() =
        runTest {
            repository.saveThemeMode(ThemeMode.LIGHT)
            setContent()
            val lightBackground = backgroundColor

            rule.onNodeWithText("ダーク").performClick()
            rule.runOnIdle {
                assertTrue(darkTheme)
                assertFalse(lightBackground == backgroundColor)
            }
            assertEquals(ThemeMode.LIGHT, repository.observeThemeMode().first())

            rule.onNodeWithText("キャンセル").performClick()
            rule.runOnIdle {
                assertFalse(darkTheme)
                assertEquals(lightBackground, backgroundColor)
                showDialog = true
            }
            rule.onNodeWithText("ダーク").performClick()
            rule.onNodeWithText("システムに従う").performClick()
            rule.runOnIdle { assertFalse(darkTheme) }
            rule.onNodeWithText("キャンセル").performClick()
            assertEquals(ThemeMode.LIGHT, repository.observeThemeMode().first())
        }

    @Test
    fun `OKでプレビュー中のテーマを保存し閉じた後も維持する`() =
        runTest {
            repository.saveThemeMode(ThemeMode.LIGHT)
            setContent()
            rule.onNodeWithText("ダーク").performClick()
            rule.onNodeWithText("OK").performClick()
            rule.runOnIdle {
                assertTrue(darkTheme)
                assertFalse(showDialog)
            }
            assertEquals(ThemeMode.DARK, repository.observeThemeMode().first())
        }

    private fun setContent() {
        rule.setContent {
            darkTheme = rememberOtherThemeDarkTheme(systemDarkTheme = false)
            KoDriverTheme(darkTheme = darkTheme) {
                backgroundColor = MaterialTheme.colorScheme.background
                Surface {
                    if (showDialog) {
                        OtherThemeDetailDialog(onDismiss = { showDialog = false })
                    }
                }
            }
        }
        rule.waitForIdle()
    }
}
