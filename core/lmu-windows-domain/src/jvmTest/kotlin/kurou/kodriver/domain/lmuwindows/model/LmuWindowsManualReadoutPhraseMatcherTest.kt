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
            matchLmuWindowsManualReadoutItemKey(listOf("燃料残量を教えて")),
        )
        assertEquals(
            LmuWindowsReadoutItemKey.MyBestLap.Root.value,
            matchLmuWindowsManualReadoutItemKey(listOf("自己ベストを教えて")),
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
    fun `英字だけの発話はLMUの語句に一致しない`() {
        assertNull(matchLmuWindowsManualReadoutItemKey(listOf("have a nice day")))
        assertNull(matchLmuWindowsManualReadoutItemKey(listOf("VE")))
    }

    @Test
    fun `空白を無視する`() {
        assertEquals(
            LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
            matchLmuWindowsManualReadoutItemKey(listOf("バーチャル エネルギー残量")),
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
    fun `タイヤの残り系の言い方はタイヤ摩耗を選ぶ`() {
        listOf(
            "タイヤ残り",
            "タイヤの残り",
            "タイヤの残量",
            "タイヤのこり",
            "タイヤのこりりょう",
        ).forEach { phrase ->
            assertEquals(
                LmuWindowsReadoutItemKey.TyreWear.Root.value,
                matchLmuWindowsManualReadoutItemKey(listOf(phrase)),
                phrase,
            )
        }
    }

    @Test
    fun `ブレーキの残り系の言い方はブレーキ摩耗を選ぶ`() {
        listOf(
            "ブレーキ残り",
            "ブレーキの残り",
            "ブレーキの残量",
            "ブレーキのこり",
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
    fun `燃料の残量やガソリン残量は残量に一致し燃料や燃料は単独では一致しない`() {
        listOf("燃料の残量", "ガソリン残量").forEach { phrase ->
            assertEquals(
                LmuWindowsReadoutItemKey.RemainingFuel.Root.value,
                matchLmuWindowsManualReadoutItemKey(listOf(phrase)),
                phrase,
            )
        }
        listOf("燃料", "燃料は").forEach { phrase ->
            assertNull(matchLmuWindowsManualReadoutItemKey(listOf(phrase)), phrase)
        }
    }

    @Test
    fun `バーチャルエネルギーの残量やエネルギー残量は残量に一致する`() {
        listOf(
            "バーチャルエネルギー残量",
            "バーチャルエネルギーの残量",
            "エネルギー残量",
            "エネルギーの残量",
        ).forEach { phrase ->
            assertEquals(
                LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value,
                matchLmuWindowsManualReadoutItemKey(listOf(phrase)),
                phrase,
            )
        }
    }

    @Test
    fun `バーチャルエネルギーの周回数やエネルギーの残り周回数は周回数項目に一致する`() {
        listOf(
            "バーチャルエネルギー周回数",
            "バーチャルエネルギーの残り周回数",
            "エネルギーの残り周回数",
            "エネルギー残り周回数",
        ).forEach { phrase ->
            assertEquals(
                LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value,
                matchLmuWindowsManualReadoutItemKey(listOf(phrase)),
                phrase,
            )
        }
    }

    @Test
    fun `単独のバーチャルエネルギーやエネルギーは残量にも周回数にも一致しない`() {
        listOf("バーチャルエネルギー", "ばーちゃるえねるぎー", "エネルギー").forEach { phrase ->
            assertNull(matchLmuWindowsManualReadoutItemKey(listOf(phrase)), phrase)
        }
    }

    @Test
    fun `一致しない・空の候補はnull`() {
        assertNull(matchLmuWindowsManualReadoutItemKey(emptyList()))
        assertNull(matchLmuWindowsManualReadoutItemKey(listOf("", "  ")))
        assertNull(matchLmuWindowsManualReadoutItemKey(listOf("こんにちは")))
    }
}
