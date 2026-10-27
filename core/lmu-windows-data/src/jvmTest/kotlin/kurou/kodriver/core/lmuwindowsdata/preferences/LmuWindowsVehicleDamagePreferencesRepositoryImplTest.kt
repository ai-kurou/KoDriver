package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsVehicleDamagePreferencesRepositoryImplTest {
    @BeforeTest
    fun setUp() {
        registerLmuWindowsReadoutItemKeys()
    }

    private val tempDir = Files.createTempDirectory("kodriver_lmu_windows_vehicle_damage_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsVehicleDamagePreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsVehicleDamagePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `enabledStates の初期値は空Map`() =
        runTest {
            assertEquals(emptyMap(), repository.observeEnabledStates().first())
        }

    @Test
    fun `saveEnabledState で保存した値を observeEnabledStates で取得できる`() =
        runTest {
            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Overheat, true)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleDamage.Overheat to true),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `saveEnabledState を複数回呼ぶと最後の値で上書きされる`() =
        runTest {
            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Overheat, true)
            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Overheat, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleDamage.Overheat to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `異なるキーで保存した値がすべて保持される`() =
        runTest {
            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Overheat, true)
            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Root, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.VehicleDamage.Overheat to true,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root to false,
                ),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `オーバーヒート文言を保存しても他の文言とスイッチは維持される`() =
        runTest {
            assertEquals("オーバーヒート", repository.observeOverheatReadoutText().first())
            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Root, false)
            listOf("自由文言", "", " ").forEach { text ->
                repository.saveOverheatReadoutText(text)
                assertEquals(text, repository.observeOverheatReadoutText().first())
                assertEquals(
                    mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleDamage.Root to false),
                    repository.observeEnabledStates().first(),
                )
                assertEquals("部品脱落", repository.observePartDetachedReadoutText().first())
                assertEquals("タイヤ脱落", repository.observeTyreDetachedReadoutText().first())
            }
        }

    @Test
    fun `部品脱落文言を保存しても他の文言とスイッチは維持される`() =
        runTest {
            assertEquals("部品脱落", repository.observePartDetachedReadoutText().first())
            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Root, false)
            listOf("自由文言", "", " ").forEach { text ->
                repository.savePartDetachedReadoutText(text)
                assertEquals(text, repository.observePartDetachedReadoutText().first())
                assertEquals(
                    mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleDamage.Root to false),
                    repository.observeEnabledStates().first(),
                )
                assertEquals("オーバーヒート", repository.observeOverheatReadoutText().first())
                assertEquals("タイヤ脱落", repository.observeTyreDetachedReadoutText().first())
            }
        }

    @Test
    fun `タイヤ脱落文言を保存しても他の文言とスイッチは維持される`() =
        runTest {
            assertEquals("タイヤ脱落", repository.observeTyreDetachedReadoutText().first())
            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Root, false)
            listOf("自由文言", "", " ").forEach { text ->
                repository.saveTyreDetachedReadoutText(text)
                assertEquals(text, repository.observeTyreDetachedReadoutText().first())
                assertEquals(
                    mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleDamage.Root to false),
                    repository.observeEnabledStates().first(),
                )
                assertEquals("オーバーヒート", repository.observeOverheatReadoutText().first())
                assertEquals("部品脱落", repository.observePartDetachedReadoutText().first())
            }
        }

    @Test
    fun `保存した設定をすべて既定値に戻せる`() =
        runTest {
            val defaultEnabledStates = repository.observeEnabledStates().first()
            val defaultOverheatReadoutText = repository.observeOverheatReadoutText().first()
            val defaultPartDetachedReadoutText = repository.observePartDetachedReadoutText().first()
            val defaultTyreDetachedReadoutText = repository.observeTyreDetachedReadoutText().first()

            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleDamage.Overheat, false)
            repository.saveOverheatReadoutText("変更した文言")
            repository.savePartDetachedReadoutText("変更した文言")
            repository.saveTyreDetachedReadoutText("変更した文言")

            assertNotEquals(LmuWindowsVehicleDamagePreferencesSerializer.defaultValue, dataStore.data.first())
            repository.resetToDefault()

            assertEquals(LmuWindowsVehicleDamagePreferencesSerializer.defaultValue, dataStore.data.first())
            assertEquals(defaultEnabledStates, repository.observeEnabledStates().first())
            assertEquals(defaultOverheatReadoutText, repository.observeOverheatReadoutText().first())
            assertEquals(defaultPartDetachedReadoutText, repository.observePartDetachedReadoutText().first())
            assertEquals(defaultTyreDetachedReadoutText, repository.observeTyreDetachedReadoutText().first())
        }
}
