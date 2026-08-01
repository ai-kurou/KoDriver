package kurou.kodriver.presentation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExitConfirmationPreferenceForExitTest {

    @Test
    fun `今後表示しないがオフなら終了確認設定を保存しない`() =
        runTest {
        var saveCallCount = 0

        saveExitConfirmationPreferenceForExit(doNotShowAgain = false) {
            saveCallCount++
        }

        assertEquals(0, saveCallCount)
    }

    @Test
    fun `今後表示しないがオンなら終了確認設定を無効で保存する`() =
        runTest {
        val savedValues = mutableListOf<Boolean>()

        saveExitConfirmationPreferenceForExit(doNotShowAgain = true) {
            savedValues += it
        }

        assertEquals(listOf(false), savedValues)
    }

    @Test
    fun `終了確認設定の保存がErrorを投げても終了処理を継続できる`() =
        runTest {
        var saveCallCount = 0

        saveExitConfirmationPreferenceForExit(doNotShowAgain = true) {
            saveCallCount++
            throw LinkageError("protobuf schema initialization failed")
        }

        assertEquals(1, saveCallCount)
    }

    @Test
    fun `終了確認設定の保存がキャンセルされた場合は再送出する`() =
        runTest {
        assertFailsWith<CancellationException> {
            saveExitConfirmationPreferenceForExit(doNotShowAgain = true) {
                throw CancellationException("cancelled")
            }
        }
    }
}
