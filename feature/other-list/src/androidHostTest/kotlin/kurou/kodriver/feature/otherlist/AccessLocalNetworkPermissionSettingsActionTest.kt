@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherlist

import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AccessLocalNetworkPermissionSettingsActionTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `呼び出すとアプリ詳細設定画面を開くIntentを起動する`() {
        rule.setContent {
            val openSettings = rememberOpenAccessLocalNetworkPermissionSettings()
            Text(
                text = "open",
                modifier = Modifier.clickable(onClick = openSettings),
            )
        }

        rule.onNode(hasText("open")).performClick()

        val shadowActivity = shadowOf(rule.activity)
        val startedIntent = shadowActivity.nextStartedActivity

        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, startedIntent.action)
        assertEquals("package:${rule.activity.packageName}", startedIntent.data.toString())
    }
}
