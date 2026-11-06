package kurou.kodriver.feature.main

import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AudioInputDevice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AudioInputDeviceSelectorTest {
    private val deviceRepository =
        FakeAudioInputDeviceRepository(listOf(AudioInputDevice("Mic A"), AudioInputDevice("Mic B")))
    private val selectedRepository = FakeSelectedAudioInputDevicePreferencesRepository()
    private val selector = audioInputDeviceSelector(deviceRepository, selectedRepository)

    @Test
    fun `未選択なら先頭を選択中にする`() =
        runTest {
            assertEquals(AudioInputDeviceList(listOf("Mic A", "Mic B"), "Mic A"), selector.refresh())
        }

    @Test
    fun `保存済みのデバイスが一覧にあれば先頭ではなくそれを選ぶ`() =
        runTest {
            selectedRepository.saved.value = "Mic B"

            assertEquals("Mic B", selector.refresh().selected)
        }

    @Test
    fun `保存済みのデバイスが一覧になければ先頭を使い保存値は書き換えない`() =
        runTest {
            selectedRepository.saved.value = "Mic X"

            assertEquals("Mic A", selector.refresh().selected)
            assertEquals("Mic X", selectedRepository.saved.value)
        }

    @Test
    fun `保存済みのデバイスが後から現れたらそれを選ぶ`() =
        runTest {
            selectedRepository.saved.value = "Mic X"
            selector.refresh()
            deviceRepository.devices = listOf(AudioInputDevice("Mic A"), AudioInputDevice("Mic X"))

            assertEquals("Mic X", selector.refresh().selected)
        }

    @Test
    fun `デバイスが0件の間は選択中なしで保存済みの名前を保持し現れたらそれを選ぶ`() =
        runTest {
            selectedRepository.saved.value = "Mic B"
            deviceRepository.devices = emptyList()

            assertEquals(AudioInputDeviceList(emptyList(), null), selector.refresh())

            deviceRepository.devices = listOf(AudioInputDevice("Mic A"), AudioInputDevice("Mic B"))
            assertEquals("Mic B", selector.refresh().selected)
        }

    @Test
    fun `デバイスを選ぶと名前を保存し以降の取得でそれを選ぶ`() =
        runTest {
            selector.select("Mic B")

            assertEquals("Mic B", selectedRepository.saved.value)
            assertEquals("Mic B", selector.refresh().selected)
        }

    @Test
    fun `保存に失敗したら選択を元に戻して例外を投げる`() =
        runTest {
            selectedRepository.saved.value = "Mic A"
            selector.refresh()
            selectedRepository.saveError = IllegalStateException("save")

            assertFailsWith<IllegalStateException> { selector.select("Mic B") }

            assertEquals("Mic A", selector.refresh().selected)
        }

    @Test
    fun `保存済みの名前を読み込む前に選んだ場合は選んだデバイスを優先する`() =
        runTest {
            selectedRepository.saved.value = "Mic A"

            selector.select("Mic B")

            assertEquals("Mic B", selector.refresh().selected)
        }

    @Test
    fun `保存値を読めなければ例外を投げ次の取得で読み直す`() =
        runTest {
            val failing = FakeSelectedAudioInputDevicePreferencesRepository(readError = IllegalStateException("read"))
            val failingSelector = audioInputDeviceSelector(deviceRepository, failing)
            assertFailsWith<IllegalStateException> { failingSelector.refresh() }

            failing.readError = null
            failing.saved.value = "Mic B"

            assertEquals("Mic B", failingSelector.refresh().selected)
        }
}
