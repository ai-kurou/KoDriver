package kurou.kodriver.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kodriver.app.shared.generated.resources.Res
import kodriver.app.shared.generated.resources.nav_more
import kurou.kodriver.feature.readout.ReadoutContent
import kurou.kodriver.feature.announcer.fakeAnnouncerDataModule
import kurou.kodriver.feature.readout.fakeReadoutDataModule
import org.jetbrains.compose.resources.stringResource
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin as koinStop

class AppScreenScreenshotTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun setUpKoin() {
            startKoin { modules(listOf(fakeReadoutDataModule, fakeAnnouncerDataModule) + appModules) }
        }

        @AfterClass
        @JvmStatic
        fun tearDownKoin() {
            koinStop()
        }
    }

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `読み上げタブ`() {
        composeRule.setContent {
            Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                AppScreen(readoutContent = {
                    ReadoutContent(scaffoldDirective = twoPaneDirective)
                })
            }
        }
        composeRule.onRoot().captureRoboImage()
    }

    @Test
    fun `その他タブ`() {
        var navMore by mutableStateOf("")

        composeRule.setContent {
            navMore = stringResource(Res.string.nav_more)
            Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                AppScreen(readoutContent = {
                    ReadoutContent(scaffoldDirective = twoPaneDirective)
                })
            }
        }
        composeRule.onNodeWithText(navMore).performClick()
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage()
    }
}
