package kurou.kodriver.feature.otherlicensedetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kurou.kodriver.buildlogic.screenshottest.composeScreenshotTest
import org.junit.Test
import kotlin.test.assertEquals

class OtherLicenseDetailPaneTest {
    @Test
    fun `ライセンス一覧を表示して戻る操作を通知する`() =
        composeScreenshotTest {
            var backCount = 0
            setContent {
                MaterialTheme {
                    OtherLicenseDetailPane(
                        canNavigateBack = true,
                        onBack = { backCount++ },
                    )
                }
            }

            onNodeWithText("ライセンス").assertIsDisplayed()
            onNode(hasContentDescription("戻る")).performClick()

            assertEquals(1, backCount)
        }
}
