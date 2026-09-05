package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.app.shared.generated.resources.Res
import kurou.kodriver.app.shared.generated.resources.banner_simulator_disconnected
import kurou.kodriver.app.shared.generated.resources.nav_log
import kurou.kodriver.app.shared.generated.resources.nav_more
import kurou.kodriver.buildlogic.screenshottest.captureRoboImage
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import kurou.kodriver.buildlogic.screenshottest.singlePaneDirective
import kurou.kodriver.feature.otherlist.OtherListUiState
import kurou.kodriver.feature.readoutlist.ReadoutContent
import kurou.kodriver.feature.readoutlist.fakeReadoutListModule
import kurou.kodriver.feature.readoutlist.readoutListModule
import kurou.kodriver.feature.telemetryloglist.TelemetryLogContent
import kurou.kodriver.feature.telemetryloglist.fakeTelemetryLogListModule
import kurou.kodriver.feature.telemetryloglist.telemetryLogListModule
import org.jetbrains.compose.resources.stringResource
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin as koinStop

class AppScreenScreenshotTest {
    companion object {
        // CI(Linux)ではDropdownMenuの展開アニメーションがデフォルトのwaitUntilタイムアウト(1000ms)より
        // 遅く完了することがあるため、余裕を持たせたタイムアウトを使う。
        private const val SIMULATOR_POPUP_WAIT_TIMEOUT_MILLIS = 5_000L

        @OptIn(ExperimentalCoroutinesApi::class)
        private val testDispatcher = UnconfinedTestDispatcher()

        @OptIn(ExperimentalCoroutinesApi::class)
        @BeforeClass
        @JvmStatic
        fun setUpKoin() {
            Dispatchers.setMain(testDispatcher)
            startKoin {
                modules(
                    listOf(
                        fakeReadoutListModule,
                        fakeTelemetryLogListModule,
                        readoutListModule,
                        telemetryLogListModule,
                    ),
                )
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        @AfterClass
        @JvmStatic
        fun tearDownKoin() {
            koinStop()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `NavigationRail ルールタブ`() =
        composeScreenshotTest {
            setAppContent {
                val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
                Box(modifier = Modifier.requiredSize(720.dp, 640.dp)) {
                    AppScreenContent(
                        layoutType = NavigationSuiteType.NavigationRail,
                        bannerUiState =
                            ConnectionBannerUiState(
                                status = ConnectionBannerStatus.DISCONNECTED,
                                message = bannerMessage,
                            ),
                        hasAppUpdate = true,
                        readoutContent = {
                            ReadoutContent(scaffoldDirective = singlePaneDirective)
                        },
                    )
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `NavigationRail その他タブ`() =
        composeScreenshotTest {
            var navMore by mutableStateOf("")

            setAppContent {
                navMore = stringResource(Res.string.nav_more)
                val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
                Box(modifier = Modifier.requiredSize(720.dp, 640.dp)) {
                    AppScreenContent(
                        layoutType = NavigationSuiteType.NavigationRail,
                        bannerUiState =
                            ConnectionBannerUiState(
                                status = ConnectionBannerStatus.DISCONNECTED,
                                message = bannerMessage,
                            ),
                        hasAppUpdate = true,
                        otherContent = { _ ->
                            OtherContent(
                                uiState = OtherListUiState(),
                                onItemSelected = {},
                                onClearSelectedItem = {},
                                scaffoldDirective = singlePaneDirective,
                            )
                        },
                    )
                }
            }
            onNodeWithText(navMore).performClick()
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun `NavigationRail ログタブ`() =
        composeScreenshotTest {
            var navLog by mutableStateOf("")

            setAppContent {
                navLog = stringResource(Res.string.nav_log)
                val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
                Box(modifier = Modifier.requiredSize(720.dp, 640.dp)) {
                    AppScreenContent(
                        layoutType = NavigationSuiteType.NavigationRail,
                        bannerUiState =
                            ConnectionBannerUiState(
                                status = ConnectionBannerStatus.DISCONNECTED,
                                message = bannerMessage,
                            ),
                        hasAppUpdate = true,
                        telemetryLogContent = { _, _ -> TelemetryLogContent() },
                    )
                }
            }
            onNodeWithText(navLog).performClick()
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun `NavigationRail シミュレータ選択ポップアップ`() =
        composeScreenshotTest {
            setAppContent {
                val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
                Box(modifier = Modifier.requiredSize(720.dp, 640.dp)) {
                    AppScreenContent(
                        layoutType = NavigationSuiteType.NavigationRail,
                        bannerUiState =
                            ConnectionBannerUiState(
                                status = ConnectionBannerStatus.DISCONNECTED,
                                message = bannerMessage,
                            ),
                        hasAppUpdate = true,
                        readoutContent = {
                            ReadoutContent(scaffoldDirective = singlePaneDirective)
                        },
                    )
                }
            }
            onNodeWithTag("primarySimulatorNavItem").performClick()
            waitUntil(timeoutMillis = SIMULATOR_POPUP_WAIT_TIMEOUT_MILLIS) {
                onAllNodesWithTag("simulatorSelectionPopup").fetchSemanticsNodes().isNotEmpty()
            }
            waitForIdle()
            onNodeWithTag("simulatorSelectionPopup").captureRoboImage()
        }

    @Test
    fun `NavigationBar ルールタブ`() =
        composeScreenshotTest {
            setAppContent {
                val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
                Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                    AppScreenContent(
                        layoutType = NavigationSuiteType.NavigationBar,
                        bannerUiState =
                            ConnectionBannerUiState(
                                status = ConnectionBannerStatus.DISCONNECTED,
                                message = bannerMessage,
                            ),
                        hasAppUpdate = true,
                        readoutContent = {
                            ReadoutContent(scaffoldDirective = singlePaneDirective)
                        },
                    )
                }
            }
            onRoot().captureRoboImage()
        }

    @Test
    fun `NavigationBar ログタブ`() =
        composeScreenshotTest {
            var navLog by mutableStateOf("")

            setAppContent {
                navLog = stringResource(Res.string.nav_log)
                val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
                Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                    AppScreenContent(
                        layoutType = NavigationSuiteType.NavigationBar,
                        bannerUiState =
                            ConnectionBannerUiState(
                                status = ConnectionBannerStatus.DISCONNECTED,
                                message = bannerMessage,
                            ),
                        hasAppUpdate = true,
                        telemetryLogContent = { _, _ -> TelemetryLogContent() },
                    )
                }
            }
            onNodeWithText(navLog).performClick()
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun `NavigationBar その他タブ`() =
        composeScreenshotTest {
            var navMore by mutableStateOf("")

            setAppContent {
                navMore = stringResource(Res.string.nav_more)
                val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
                Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                    AppScreenContent(
                        layoutType = NavigationSuiteType.NavigationBar,
                        bannerUiState =
                            ConnectionBannerUiState(
                                status = ConnectionBannerStatus.DISCONNECTED,
                                message = bannerMessage,
                            ),
                        hasAppUpdate = true,
                        otherContent = { _ ->
                            OtherContent(
                                uiState = OtherListUiState(),
                                onItemSelected = {},
                                onClearSelectedItem = {},
                                scaffoldDirective = singlePaneDirective,
                            )
                        },
                    )
                }
            }
            onNodeWithText(navMore).performClick()
            waitForIdle()
            onRoot().captureRoboImage()
        }

    @Test
    fun `NavigationBar シミュレータ選択ポップアップ`() =
        composeScreenshotTest {
            setAppContent {
                val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
                Box(modifier = Modifier.requiredSize(360.dp, 640.dp)) {
                    AppScreenContent(
                        layoutType = NavigationSuiteType.NavigationBar,
                        bannerUiState =
                            ConnectionBannerUiState(
                                status = ConnectionBannerStatus.DISCONNECTED,
                                message = bannerMessage,
                            ),
                        hasAppUpdate = true,
                        readoutContent = {
                            ReadoutContent(scaffoldDirective = singlePaneDirective)
                        },
                    )
                }
            }
            onNodeWithTag("primarySimulatorNavItem").performClick()
            waitUntil(timeoutMillis = SIMULATOR_POPUP_WAIT_TIMEOUT_MILLIS) {
                onAllNodesWithTag("simulatorSelectionPopup").fetchSemanticsNodes().isNotEmpty()
            }
            waitForIdle()
            onNodeWithTag("simulatorSelectionPopup").captureRoboImage()
        }

    @Test
    fun `アップデートバッジ非表示`() =
        composeScreenshotTest {
            setAppContent {
                val bannerMessage = stringResource(Res.string.banner_simulator_disconnected)
                Box(modifier = Modifier.requiredSize(720.dp, 640.dp)) {
                    AppScreenContent(
                        layoutType = NavigationSuiteType.NavigationRail,
                        bannerUiState =
                            ConnectionBannerUiState(
                                status = ConnectionBannerStatus.DISCONNECTED,
                                message = bannerMessage,
                            ),
                        hasAppUpdate = false,
                        readoutContent = {
                            ReadoutContent(scaffoldDirective = singlePaneDirective)
                        },
                    )
                }
            }
            onRoot().captureRoboImage()
        }

    private fun DesktopComposeUiTest.setAppContent(content: @Composable () -> Unit) {
        setContent {
            AppTheme {
                content()
            }
        }
    }
}
