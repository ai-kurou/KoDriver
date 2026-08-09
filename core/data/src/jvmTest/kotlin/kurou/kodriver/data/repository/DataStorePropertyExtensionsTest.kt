package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DataStorePropertyExtensionsTest {
    @Serializable
    private data class FakePreferences(
        val value: Int = 0,
    )

    private val tempDir = Files.createTempDirectory("kodriver_data_store_property_extensions_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer =
                object : Serializer<FakePreferences> {
                    override val defaultValue: FakePreferences = FakePreferences()

                    override suspend fun readFrom(input: InputStream): FakePreferences =
                        ProtoBuf.decodeFromByteArray(FakePreferences.serializer(), input.readBytes())

                    override suspend fun writeTo(
                        t: FakePreferences,
                        output: OutputStream,
                    ) {
                        output.write(ProtoBuf.encodeToByteArray(FakePreferences.serializer(), t))
                    }
                },
            scope = testScope,
            produceFile = { tempDir.resolve("test.pb") },
        )

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `observePropertyでDataStoreの値からプロパティを取得できる`() =
        testScope.runTest {
            assertEquals(0, dataStore.observeProperty { it.value }.first())
        }

    @Test
    fun `savePropertyで保存した値をobservePropertyで取得できる`() =
        testScope.runTest {
            dataStore.saveProperty(42) { preferences, value -> preferences.copy(value = value) }

            assertEquals(42, dataStore.observeProperty { it.value }.first())
        }
}
