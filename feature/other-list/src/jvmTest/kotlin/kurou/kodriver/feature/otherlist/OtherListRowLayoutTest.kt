package kurou.kodriver.feature.otherlist

import org.junit.Test
import kotlin.test.assertEquals

class OtherListRowLayoutTest {
    @Test
    fun `項目がなければ行もLazyListインデックスも空になる`() {
        val layout = otherListRowLayout(groupedItems = emptyMap(), appVersionVisible = true)

        assertEquals(emptyList(), layout.itemKeys)
        assertEquals(emptyList(), layout.lazyIndexes)
    }

    @Test
    fun `見出しと項目を数えて各項目のLazyListインデックスを求める`() {
        val groupedItems =
            mapOf(
                OtherListSection.ConnectionSettings to listOf(OtherListItemType.ServerIp),
                OtherListSection.ReadoutSettings to listOf(OtherListItemType.Volume, OtherListItemType.Voice),
                OtherListSection.Information to listOf(OtherListItemType.License),
                OtherListSection.DataLog to listOf(OtherListItemType.ShowLogs),
            )

        val layout = otherListRowLayout(groupedItems = groupedItems, appVersionVisible = true)

        // 0: 接続の見出し, 1: IP, 2: 読み上げの見出し, 3: 音量, 4: 音声,
        // 5: 情報の見出し, 6: ライセンス, 7: アプリバージョン, 8: データ・ログの見出し, 9: ログを表示
        assertEquals(
            listOf(
                OtherListItemType.ServerIp.id,
                OtherListItemType.Volume.id,
                OtherListItemType.Voice.id,
                OtherListItemType.License.id,
                OTHER_APP_VERSION_ROW_KEY,
                OtherListItemType.ShowLogs.id,
            ),
            layout.itemKeys,
        )
        assertEquals(listOf(1, 3, 4, 6, 7, 9), layout.lazyIndexes)
    }

    @Test
    fun `アプリバージョンを表示しない場合は行に含めず後続の項目は詰めて数える`() {
        val groupedItems =
            mapOf(
                OtherListSection.Information to listOf(OtherListItemType.License),
                OtherListSection.DataLog to listOf(OtherListItemType.ShowLogs),
            )

        val layout = otherListRowLayout(groupedItems = groupedItems, appVersionVisible = false)

        assertEquals(listOf(OtherListItemType.License.id, OtherListItemType.ShowLogs.id), layout.itemKeys)
        assertEquals(listOf(1, 3), layout.lazyIndexes)
    }

    @Test
    fun `情報セクションに項目がなければアプリバージョンを表示対象でも行に含めない`() {
        val groupedItems = mapOf(OtherListSection.ReadoutSettings to listOf(OtherListItemType.Volume))

        val layout = otherListRowLayout(groupedItems = groupedItems, appVersionVisible = true)

        assertEquals(listOf(OtherListItemType.Volume.id), layout.itemKeys)
        assertEquals(listOf(1), layout.lazyIndexes)
    }
}
