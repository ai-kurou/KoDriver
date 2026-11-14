package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.ReadoutItemKeyRegistry
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class LmuWindowsManualReadoutLegacyMigrationTest {
    private val ahead = "lmu_windows_auto_class_car_gap_ahead"
    private val behind = "lmu_windows_auto_class_car_gap_behind"
    private val merged = "lmu_windows_auto_class_car_gap"

    @Test
    fun `旧キーがないセットはそのまま返す`() {
        val set = ManualReadoutSet(id = "s", itemKeys = listOf("a", merged), itemTexts = mapOf("a" to "x"))
        assertSame(set, migrateLmuWindowsManualReadoutLegacyClassCarGap(set))
    }

    @Test
    fun `前方だけの旧キーは位置を保って新キーへ置き換える`() {
        val set = ManualReadoutSet(id = "s", itemKeys = listOf("a", ahead, "b"))
        assertEquals(listOf("a", merged, "b"), migrateLmuWindowsManualReadoutLegacyClassCarGap(set).itemKeys)
    }

    @Test
    fun `後方だけの旧キーも新キーへ置き換える`() {
        val set = ManualReadoutSet(id = "s", itemKeys = listOf(behind, "b"))
        assertEquals(listOf(merged, "b"), migrateLmuWindowsManualReadoutLegacyClassCarGap(set).itemKeys)
    }

    @Test
    fun `前方と後方の両方があっても最初の位置に1つへまとめる`() {
        val set = ManualReadoutSet(id = "s", itemKeys = listOf(behind, "a", ahead))
        assertEquals(listOf(merged, "a"), migrateLmuWindowsManualReadoutLegacyClassCarGap(set).itemKeys)
    }

    @Test
    fun `すでに新キーがあれば重複させず旧キーの文言は破棄する`() {
        val set =
            ManualReadoutSet(
                id = "s",
                itemKeys = listOf(merged, ahead),
                itemTexts = mapOf(ahead to "クラス内前方{seconds}秒", behind to "x", "a" to "keep", merged to "{ahead}"),
            )
        val migrated = migrateLmuWindowsManualReadoutLegacyClassCarGap(set)
        assertEquals(listOf(merged), migrated.itemKeys)
        assertEquals(mapOf("a" to "keep", merged to "{ahead}"), migrated.itemTexts)
    }

    @Test
    fun `項目に残っていない旧キーの文言だけでも破棄する`() {
        val set = ManualReadoutSet(id = "s", itemKeys = listOf("a"), itemTexts = mapOf(ahead to "x"))
        assertEquals(emptyMap(), migrateLmuWindowsManualReadoutLegacyClassCarGap(set).itemTexts)
    }

    @Test
    fun `LMU の手動読み上げ spec から移行処理を使える`() {
        val spec = ReadoutItemKeyRegistry.manualReadoutSpecOf(Simulator.LmuWindows.id)
        val migrated = spec?.migrateLegacySet?.invoke(ManualReadoutSet(id = "s", itemKeys = listOf(ahead)))
        assertEquals(listOf(merged), migrated?.itemKeys)
    }
}
