package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.ManualReadoutSet
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ManualReadoutSetPreferencesRepositoryImplTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_manual_readout_set_preferences_test")
            .toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = ManualReadoutSetPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = ManualReadoutSetPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `セット名を追加と更新と一覧保存で保持し旧データは名前なしになる`() =
        runTest {
            dataStore.updateData {
                ManualReadoutSetPreferences(listOf(ManualReadoutSetPreference("legacy")))
            }
            assertEquals(
                "",
                repository
                    .observeSets()
                    .first()
                    .single()
                    .name,
            )
            val named = ManualReadoutSet("named", name = "レース用", number = 7)
            repository.addSet(named)
            assertEquals(named, repository.observeSets().first().last())
            val renamed = named.copy(name = "予選用")
            repository.updateSet(renamed)
            assertEquals(renamed, repository.observeSets().first().last())
            repository.saveSets(listOf(named, renamed.copy(id = "duplicate", name = named.name)))
            assertEquals(
                listOf(named, renamed.copy(id = "duplicate", name = named.name)),
                repository.observeSets().first(),
            )
        }

    @Test
    fun `空の保存済み一覧へセットを追加できる`() =
        runTest {
            val set = ManualReadoutSet("set-1", listOf("SESSION", "REMOVED_KEY"), mapOf("SESSION" to "残量{percent}%"))

            repository.addSet(set)

            assertEquals(listOf(set.copy(number = 1)), repository.observeSets().first())
        }

    @Test
    fun `未採番の既存セットの位置番号と重ならないよう採番し連続追加しても保持する`() =
        runTest {
            val existing = ManualReadoutSet("existing", listOf("SESSION", "REMOVED_KEY", "SIMULATOR"))
            val first = ManualReadoutSet("first")
            val second = ManualReadoutSet("second", listOf("BEST_LAP"))
            repository.saveSets(listOf(existing))

            repository.addSet(first)
            repository.addSet(second)

            assertEquals(
                listOf(existing, first.copy(number = 2), second.copy(number = 3)),
                repository.observeSets().first(),
            )
        }

    @Test
    fun `並行追加しても保存済みセットと全追加セットを保持する`() =
        runTest {
            val existing = ManualReadoutSet("existing", listOf("SESSION"))
            val added = (1..10).map { ManualReadoutSet("set-$it", listOf("BEST_LAP")) }
            repository.saveSets(listOf(existing))

            coroutineScope {
                added.forEach { set -> launch { repository.addSet(set) } }
            }

            val saved = repository.observeSets().first()
            assertEquals(existing, saved.first())
            assertEquals(added.size + 1, saved.size)
            assertEquals(added.toSet(), saved.drop(1).map { it.copy(number = 0) }.toSet())
            assertEquals((2..11).toSet(), saved.drop(1).map { it.number }.toSet())
        }

    @Test
    fun `未知キーを含むセットをそのまま保存して読み出せる`() =
        runTest {
            val sets =
                listOf(
                    ManualReadoutSet(
                        "set-1",
                        listOf("BRAKE_WEAR", "BEST_LAP", "REMOVED_KEY", "SIMULATOR"),
                        mapOf("REMOVED_KEY" to "", "BEST_LAP" to "文言"),
                    ),
                    ManualReadoutSet("set-2"),
                )
            repository.saveSets(sets)

            assertEquals(sets, repository.observeSets().first())
        }

    @Test
    fun `初期値は空リスト・保存したセットを読み出せる・上書きで更新される`() =
        runTest {
            assertEquals(emptyList(), repository.observeSets().first())

            repository.saveSets(
                listOf(ManualReadoutSet("set-1", listOf("SESSION", "SIMULATOR")), ManualReadoutSet("set-2")),
            )
            assertEquals(
                listOf(ManualReadoutSet("set-1", listOf("SESSION", "SIMULATOR")), ManualReadoutSet("set-2")),
                repository.observeSets().first(),
            )

            repository.saveSets(
                listOf(ManualReadoutSet("set-1", listOf("SIMULATOR", "SESSION")), ManualReadoutSet("set-2")),
            )
            assertEquals(
                listOf(ManualReadoutSet("set-1", listOf("SIMULATOR", "SESSION")), ManualReadoutSet("set-2")),
                repository.observeSets().first(),
            )

            repository.saveSets(emptyList())
            assertEquals(emptyList(), repository.observeSets().first())
        }

    @Test
    fun `保存形式の未知キーもそのままドメインモデルへ変換する`() =
        runTest {
            dataStore.updateData {
                it.copy(
                    sets =
                        listOf(
                            ManualReadoutSetPreference("set-1", listOf("SESSION", "REMOVED_KEY", "SIMULATOR")),
                            ManualReadoutSetPreference("set-2"),
                        ),
                )
            }

            assertEquals(
                listOf(
                    ManualReadoutSet("set-1", listOf("SESSION", "REMOVED_KEY", "SIMULATOR")),
                    ManualReadoutSet("set-2"),
                ),
                repository.observeSets().first(),
            )
        }

    @Test
    fun `更新と削除は対象IDだけに作用し存在しないIDなら一覧を保持する`() =
        runTest {
            val first = ManualReadoutSet("first", listOf("UNKNOWN"), mapOf("UNKNOWN" to "保持"))
            val second = ManualReadoutSet("second")
            val updated =
                second.copy(itemKeys = listOf("ENERGY", "UNKNOWN"), itemTexts = mapOf("ENERGY" to "残量{percent}%"))
            repository.saveSets(listOf(first, second))

            repository.updateSet(updated)
            repository.updateSet(ManualReadoutSet("missing"))
            repository.deleteSet("missing")
            assertEquals(listOf(first, updated), repository.observeSets().first())

            repository.deleteSet(second.id)
            assertEquals(listOf(first), repository.observeSets().first())
            repository.deleteSet(first.id)
            assertEquals(emptyList(), repository.observeSets().first())
        }

    @Test
    fun `並行する追加と別セットの更新と削除が互いの変更を失わない`() =
        runTest {
            val updated = ManualReadoutSet("update", listOf("ENERGY", "UNKNOWN"))
            val removed = ManualReadoutSet("remove")
            val added = ManualReadoutSet("added")
            repository.saveSets(listOf(updated.copy(itemKeys = emptyList()), removed))

            coroutineScope {
                launch { repository.updateSet(updated) }
                launch { repository.deleteSet(removed.id) }
                launch { repository.addSet(added) }
            }

            val saved = repository.observeSets().first()
            // 追加が削除の前後どちらで走るかで、未採番の既存セットを含む保存数が変わるため番号は2か3になる。
            assertTrue(saved.last().number in 2..3)
            assertEquals(listOf(updated, added.copy(number = saved.last().number)), saved)
        }

    @Test
    fun `追加と更新と一覧保存でホットキーと割り当てなしを読み戻せる`() =
        runTest {
            val assigned =
                ManualReadoutSet(
                    "target",
                    number = 1,
                    hotkeyVirtualKeyCode = 0x30,
                    hotkeyModifiers = ManualReadoutHotkeyModifiers.CTRL_ALT,
                )
            repository.addSet(assigned)
            assertEquals(listOf(assigned), repository.observeSets().first())
            val updated =
                assigned.copy(
                    hotkeyVirtualKeyCode = 0x5A,
                    hotkeyModifiers = ManualReadoutHotkeyModifiers.ALT_SHIFT,
                )
            repository.updateSet(updated)
            assertEquals(listOf(updated), repository.observeSets().first())
            val cleared = updated.copy(hotkeyVirtualKeyCode = null)
            repository.updateSet(cleared)
            assertEquals(listOf(cleared), repository.observeSets().first())
            repository.saveSets(listOf(updated, ManualReadoutSet("none")))
            assertEquals(listOf(updated, ManualReadoutSet("none")), repository.observeSets().first())
        }

    @Test
    fun `削除しても既存番号を保持し最大番号の次を採番する`() =
        runTest {
            (1..3).forEach { repository.addSet(ManualReadoutSet("set-$it")) }
            repository.deleteSet("set-2")
            repository.addSet(ManualReadoutSet("set-4"))
            assertEquals(
                listOf(
                    ManualReadoutSet("set-1", number = 1),
                    ManualReadoutSet("set-3", number = 3),
                    ManualReadoutSet("set-4", number = 4),
                ),
                repository.observeSets().first(),
            )
        }

    @Test
    fun `旧データと未知の修飾キーは既定値で読み出す`() =
        runTest {
            dataStore.updateData {
                ManualReadoutSetPreferences(
                    listOf(
                        ManualReadoutSetPreference("legacy", hotkeyVirtualKeyCode = 0x41),
                        ManualReadoutSetPreference("unknown", hotkeyModifiers = 99),
                        ManualReadoutSetPreference("removed", hotkeyModifiers = 7),
                    ),
                )
            }
            assertEquals(
                listOf(
                    ManualReadoutSet("legacy", hotkeyVirtualKeyCode = 0x41),
                    ManualReadoutSet("unknown"),
                    ManualReadoutSet("removed", hotkeyModifiers = ManualReadoutHotkeyModifiers.CTRL_ALT),
                ),
                repository.observeSets().first(),
            )
        }
}
