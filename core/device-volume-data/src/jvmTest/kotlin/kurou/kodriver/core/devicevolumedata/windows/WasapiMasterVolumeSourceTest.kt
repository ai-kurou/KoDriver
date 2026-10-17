package kurou.kodriver.core.devicevolumedata.windows

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class WasapiMasterVolumeSourceTest {
    @Test
    fun `取得値を返してCOMリソースを逆順に解放する`() {
        val api = FakeWasapiNativeApi()

        assertEquals(0.5f, WasapiMasterVolumeSource(api).getScalarVolume())

        assertEquals(
            listOf(
                "initialize",
                "create",
                "device",
                "activate",
                "get",
                "releaseEndpoint",
                "releaseDevice",
                "releaseEnumerator",
                "uninitialize",
            ),
            api.calls,
        )
    }

    @Test
    fun `設定値をそのまま渡してCOMリソースを逆順に解放する`() {
        val api = FakeWasapiNativeApi()

        WasapiMasterVolumeSource(api).setScalarVolume(0.25f)

        assertEquals(0.25f, api.scalarVolume)
        assertEquals(
            listOf(
                "initialize",
                "create",
                "device",
                "activate",
                "set",
                "releaseEndpoint",
                "releaseDevice",
                "releaseEnumerator",
                "uninitialize",
            ),
            api.calls,
        )
    }

    @Test
    fun `COM初期化が不成功なら音量操作後も終了処理を呼ばない`() {
        val api = FakeWasapiNativeApi(initialized = false)

        assertEquals(0.5f, WasapiMasterVolumeSource(api).getScalarVolume())

        assertEquals(
            listOf(
                "initialize",
                "create",
                "device",
                "activate",
                "get",
                "releaseEndpoint",
                "releaseDevice",
                "releaseEnumerator",
            ),
            api.calls,
        )
    }

    @Test
    fun `初期化の例外時は後続処理を呼ばない`() {
        val api = FakeWasapiNativeApi(failingCall = "initialize")

        val thrown =
            assertFailsWith<IllegalStateException> {
                WasapiMasterVolumeSource(api).getScalarVolume()
            }

        assertSame(api.failure, thrown)
        assertEquals(
            listOf("initialize"),
            api.calls,
        )
    }

    @Test
    fun `列挙子生成の例外時はCOMのみ終了する`() {
        val api = FakeWasapiNativeApi(failingCall = "create")

        val thrown =
            assertFailsWith<IllegalStateException> {
                WasapiMasterVolumeSource(api).getScalarVolume()
            }

        assertSame(api.failure, thrown)
        assertEquals(
            listOf("initialize", "create", "uninitialize"),
            api.calls,
        )
    }

    @Test
    fun `デバイス取得の例外時は列挙子を解放する`() {
        val api = FakeWasapiNativeApi(failingCall = "device")

        val thrown =
            assertFailsWith<IllegalStateException> {
                WasapiMasterVolumeSource(api).getScalarVolume()
            }

        assertSame(api.failure, thrown)
        assertEquals(
            listOf("initialize", "create", "device", "releaseEnumerator", "uninitialize"),
            api.calls,
        )
    }

    @Test
    fun `有効化の例外時はデバイスと列挙子を解放する`() {
        val api = FakeWasapiNativeApi(failingCall = "activate")

        val thrown =
            assertFailsWith<IllegalStateException> {
                WasapiMasterVolumeSource(api).getScalarVolume()
            }

        assertSame(api.failure, thrown)
        assertEquals(
            listOf("initialize", "create", "device", "activate", "releaseDevice", "releaseEnumerator", "uninitialize"),
            api.calls,
        )
    }

    @Test
    fun `音量取得の例外時も全リソースを解放する`() {
        val api = FakeWasapiNativeApi(failingCall = "get")

        val thrown =
            assertFailsWith<IllegalStateException> {
                WasapiMasterVolumeSource(api).getScalarVolume()
            }

        assertSame(api.failure, thrown)
        assertEquals(
            listOf(
                "initialize",
                "create",
                "device",
                "activate",
                "get",
                "releaseEndpoint",
                "releaseDevice",
                "releaseEnumerator",
                "uninitialize",
            ),
            api.calls,
        )
    }

    @Test
    fun `音量設定の例外時も全リソースを解放する`() {
        val api = FakeWasapiNativeApi(failingCall = "set")

        val thrown =
            assertFailsWith<IllegalStateException> {
                WasapiMasterVolumeSource(api).setScalarVolume(0.25f)
            }

        assertSame(api.failure, thrown)
        assertEquals(
            listOf(
                "initialize",
                "create",
                "device",
                "activate",
                "set",
                "releaseEndpoint",
                "releaseDevice",
                "releaseEnumerator",
                "uninitialize",
            ),
            api.calls,
        )
    }

    @Test
    fun `COM初期化が不成功なら後続の例外時も終了処理を呼ばない`() {
        val api = FakeWasapiNativeApi(initialized = false, failingCall = "create")

        val thrown =
            assertFailsWith<IllegalStateException> {
                WasapiMasterVolumeSource(api).getScalarVolume()
            }

        assertSame(api.failure, thrown)
        assertEquals(listOf("initialize", "create"), api.calls)
    }
}
