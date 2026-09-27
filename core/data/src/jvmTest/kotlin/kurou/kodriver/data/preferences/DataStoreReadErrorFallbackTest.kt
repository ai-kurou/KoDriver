package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DataStoreReadErrorFallbackTest {
    private data class FakePreferences(
        val value: Int = 0,
    )

    private class FakeDataStore<T>(
        override val data: Flow<T>,
    ) : DataStore<T> {
        var updatedValue: T? = null

        override suspend fun updateData(transform: suspend (t: T) -> T): T =
            transform(data.toList().last()).also { updatedValue = it }
    }

    @Test
    fun `IOExceptionのときデフォルト値を1回emitして正常終了する`() =
        runTest {
            val flow =
                flow {
                    emit(FakePreferences(value = 1))
                    throw IOException("read failed")
                }

            val emitted = flow.fallbackOnReadError(FakePreferences()).toList()

            assertEquals(listOf(FakePreferences(value = 1), FakePreferences()), emitted)
        }

    @Test
    fun `IOException以外の例外はそのまま伝播する`() =
        runTest {
            val flow = flow<FakePreferences> { throw IllegalStateException("boom") }

            assertFailsWith<IllegalStateException> {
                flow.fallbackOnReadError(FakePreferences()).toList()
            }
        }

    @Test
    fun `正常時は元の値がそのまま流れる`() =
        runTest {
            val flow = flow { emit(FakePreferences(value = 2)) }

            assertEquals(listOf(FakePreferences(value = 2)), flow.fallbackOnReadError(FakePreferences()).toList())
        }

    @Test
    fun `デコレータのdataは読み取り失敗時にデフォルト値を返す`() =
        runTest {
            val delegate = FakeDataStore<FakePreferences>(flow { throw IOException("read failed") })

            val dataStore = FallbackOnReadErrorDataStore(delegate, FakePreferences(value = 3))

            assertEquals(listOf(FakePreferences(value = 3)), dataStore.data.toList())
        }

    @Test
    fun `デコレータのupdateDataは委譲先をそのまま呼ぶ`() =
        runTest {
            val delegate = FakeDataStore(flow { emit(FakePreferences(value = 1)) })

            val dataStore = FallbackOnReadErrorDataStore(delegate, FakePreferences())
            val updated = dataStore.updateData { it.copy(value = it.value + 1) }

            assertEquals(FakePreferences(value = 2), updated)
            assertEquals(FakePreferences(value = 2), delegate.updatedValue)
        }
}
