@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherlist

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OtherListItemsTest {
    @Test
    fun `AndroidではAndroid向けTTS案内だけが一覧に含まれる`() {
        val items = buildOtherListItems()

        assertTrue(items.contains(OtherListItemType.TtsEngineMissing))
        assertTrue(items.contains(OtherListItemType.TtsLanguageDataMissing))
        assertFalse(items.contains(OtherListItemType.WindowsSpeechUnavailable))
    }

    @Test
    fun `Android16以上ではオーバーレイ設定項目とStartupとDebugStateを除いた全項目を定義順で返す`() {
        val items = buildOtherListItems()

        assertEquals(
            listOf(
                OtherListItemType.AccessLocalNetworkPermission,
                OtherListItemType.ServerIp,
                OtherListItemType.ConsoleIp,
                OtherListItemType.Volume,
                OtherListItemType.TtsEngineMissing,
                OtherListItemType.TtsLanguageDataMissing,
                OtherListItemType.KeepScreenOn,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.Voice,
                OtherListItemType.Theme,
                OtherListItemType.DynamicColor,
                OtherListItemType.HapticFeedback,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.Feedback,
                OtherListItemType.License,
            ),
            items,
        )
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OtherListItemsAndroid15Test {
    @Test
    fun `Android16未満ではAccessLocalNetworkPermissionとオーバーレイ設定項目とStartupとDebugStateを除いた全項目を定義順で返す`() {
        val items = buildOtherListItems()

        assertEquals(
            listOf(
                OtherListItemType.ServerIp,
                OtherListItemType.ConsoleIp,
                OtherListItemType.Volume,
                OtherListItemType.TtsEngineMissing,
                OtherListItemType.TtsLanguageDataMissing,
                OtherListItemType.KeepScreenOn,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.Voice,
                OtherListItemType.Theme,
                OtherListItemType.DynamicColor,
                OtherListItemType.HapticFeedback,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.Feedback,
                OtherListItemType.License,
            ),
            items,
        )
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class OtherListItemsAndroid11Test {
    @Test
    fun `Android12未満ではDynamicColorとAccessLocalNetworkPermissionとオーバーレイ設定項目とStartupとDebugStateを除いた全項目を定義順で返す`() {
        val items = buildOtherListItems()

        assertEquals(
            listOf(
                OtherListItemType.ServerIp,
                OtherListItemType.ConsoleIp,
                OtherListItemType.Volume,
                OtherListItemType.TtsEngineMissing,
                OtherListItemType.TtsLanguageDataMissing,
                OtherListItemType.KeepScreenOn,
                OtherListItemType.ReadoutStartSound,
                OtherListItemType.Voice,
                OtherListItemType.Theme,
                OtherListItemType.HapticFeedback,
                OtherListItemType.GitHubRepository,
                OtherListItemType.ReleasePage,
                OtherListItemType.Feedback,
                OtherListItemType.License,
            ),
            items,
        )
    }
}
