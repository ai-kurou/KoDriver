package kurou.kodriver.domain.util

import kotlin.test.Test
import kotlin.test.assertTrue

class AppVersionTest {
    @Test
    fun `APP_VERSIONはメジャー・マイナー・パッチ形式のバージョン文字列である`() {
        assertTrue(Regex("""\d+\.\d+\.\d+""").matches(APP_VERSION))
    }
}
