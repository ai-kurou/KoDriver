package kurou.kodriver.core.lmuwindowsrestapidata

import kurou.kodriver.domain.repository.LmuWindowsBrakeWearRepository
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertSame

class LmuWindowsRestApiDataModuleTest {
    @Test
    fun `ブレーキ摩耗Repositoryがシングルトンとして解決できる`() {
        val koin = koinApplication { modules(lmuWindowsRestApiDataModule) }.koin

        val first = koin.get<LmuWindowsBrakeWearRepository>()
        val second = koin.get<LmuWindowsBrakeWearRepository>()

        assertSame(first, second)
        koin.close()
    }
}
