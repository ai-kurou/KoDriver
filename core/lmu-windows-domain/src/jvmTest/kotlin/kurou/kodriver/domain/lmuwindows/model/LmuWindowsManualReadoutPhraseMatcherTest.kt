package kurou.kodriver.domain.lmuwindows.model

import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LmuWindowsManualReadoutPhraseMatcherTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerLmuWindowsReadoutItemKeys()
    }

    @Test
    fun `語句に一致する発話は対応する項目キーを返す`() {
        assertEquals(
            LmuWindowsReadoutItemKey.RemainingFuel.Root.value,
            matchLmuWindowsManualReadoutItemKey(listOf("燃料を教えて")),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.MyBestLap.Root.value,
            matchLmuWindowsManualReadoutItemKey(listOf("じこべ")),
        )
    }

    @Test
    fun `複数項目に一致する場合は最も長い語句の項目を選ぶ`() {
        assertEquals(
            LmuWindowsReadoutItemKey.RemainingFuel.Laps.value,
            matchLmuWindowsManualReadoutItemKey(listOf("燃料周回を教えて")),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value,
            matchLmuWindowsManualReadoutItemKey(listOf("バーチャルエネルギー周回")),
        )
    }

    @Test
    fun `燃料はあと何周のような助詞付きの周回数の質問は燃料残量より優先する`() {
        assertEquals(
            LmuWindowsReadoutItemKey.RemainingFuel.Laps.value,
            matchLmuWindowsManualReadoutItemKey(listOf("燃料はあと何周")),
        )
    }

    @Test
    fun `英字だけの語句は英単語の途中には一致せず単独や日本語に隣接する場合は一致する`() {
        assertNull(matchLmuWindowsManualReadoutItemKey(listOf("have a nice day")))
        assertEquals(
            LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
            matchLmuWindowsManualReadoutItemKey(listOf("VEを教えて")),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
            matchLmuWindowsManualReadoutItemKey(listOf("have", "VE")),
        )
    }

    @Test
    fun `空白と英字の大文字小文字を無視する`() {
        assertEquals(
            LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
            matchLmuWindowsManualReadoutItemKey(listOf("v e")),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.TyreTemperature.Root.value,
            matchLmuWindowsManualReadoutItemKey(listOf("タイヤ 温度")),
        )
    }

    @Test
    fun `候補は先頭から見て最初に一致した候補を採用する`() {
        assertEquals(
            LmuWindowsReadoutItemKey.BrakeTemperature.Root.value,
            matchLmuWindowsManualReadoutItemKey(listOf("関係ない言葉", "ブレーキ温度", "燃料")),
        )
    }

    @Test
    fun `タイヤの残り・減り系の言い方はタイヤ摩耗を選ぶ`() {
        listOf(
            "タイヤ残り",
            "タイヤの残り",
            "タイヤの残量",
            "タイヤ減り",
            "タイヤのへり",
            "タイヤすり減り",
            "タイヤの減り具合",
            "タイヤのこり",
            "タイヤのこりりょう",
            "タイヤへり",
        ).forEach { phrase ->
            assertEquals(
                LmuWindowsReadoutItemKey.TyreWear.Root.value,
                matchLmuWindowsManualReadoutItemKey(listOf(phrase)),
                phrase,
            )
        }
    }

    @Test
    fun `ブレーキの残り・減り系の言い方はブレーキ摩耗を選ぶ`() {
        listOf(
            "ブレーキ残り",
            "ブレーキの残り",
            "ブレーキの残量",
            "ブレーキ減り",
            "ブレーキのへり",
            "ブレーキすり減り",
            "ブレーキの減り具合",
            "ブレーキのこり",
            "ブレーキのこりりょう",
            "ブレーキへり",
        ).forEach { phrase ->
            assertEquals(
                LmuWindowsReadoutItemKey.BrakeWear.Root.value,
                matchLmuWindowsManualReadoutItemKey(listOf(phrase)),
                phrase,
            )
        }
    }

    @Test
    fun `タイヤの残りを含む周回数の言い方はタイヤ摩耗ではなく周回数項目を選ぶ`() {
        listOf(
            "タイヤ残り周回",
            "タイヤの残り周回",
            "タイヤのこり周回",
            "タイヤ残りあと何周",
            "タイヤの残りあと何周",
            "タイヤのこりあと何周",
            "タイヤ残り何周",
            "タイヤの残り何周",
            "タイヤ残りは何周",
            "タイヤの残りは何周",
            "タイヤ残りラップ",
            "タイヤの残りラップ",
        ).forEach { phrase ->
            assertEquals(
                LmuWindowsReadoutItemKey.PitTiming.TyreWear.value,
                matchLmuWindowsManualReadoutItemKey(listOf(phrase)),
                phrase,
            )
        }
    }

    @Test
    fun `一致しない・空の候補はnull`() {
        assertNull(matchLmuWindowsManualReadoutItemKey(emptyList()))
        assertNull(matchLmuWindowsManualReadoutItemKey(listOf("", "  ")))
        assertNull(matchLmuWindowsManualReadoutItemKey(listOf("こんにちは")))
    }
}
