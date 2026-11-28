@file:Suppress("FunctionNaming")

package kurou.kodriver.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidPreferencesCorruptionHandlerTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dataStoreScope = CoroutineScope(testDispatcher + SupervisorJob())
    private lateinit var tempFile: File

    @BeforeTest
    fun setUp() {
        tempFile = File.createTempFile("corrupted_test", ".preferences_pb")
        // 長さ127のlength-delimitedフィールドに対してデータが足りない破損ファイル
        tempFile.writeBytes(byteArrayOf(0x0A, 0x7F))
    }

    @AfterTest
    fun tearDown() {
        runBlocking { dataStoreScope.coroutineContext.job.cancelAndJoin() }
        tempFile.delete()
    }

    @Test
    fun `破損ファイルは空のPreferencesで読め、保存後の値を読み直せる`() =
        runTest(testDispatcher) {
            val dataStore =
                PreferenceDataStoreFactory.create(
                    scope = dataStoreScope,
                    produceFile = { tempFile },
                    corruptionHandler = androidPreferencesCorruptionHandler,
                )
            val key = intPreferencesKey("value")
            assertEquals(null, dataStore.data.first()[key])

            dataStore.edit { it[key] = 1 }

            assertEquals(1, dataStore.data.first()[key])
            assertTrue(tempFile.exists())
        }
}
