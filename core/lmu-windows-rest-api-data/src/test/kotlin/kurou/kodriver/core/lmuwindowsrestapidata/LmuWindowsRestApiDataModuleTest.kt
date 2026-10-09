package kurou.kodriver.core.lmuwindowsrestapidata

import kurou.kodriver.domain.repository.LmuWindowsBrakeWearInvestigationRepository
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertSame

class LmuWindowsRestApiDataModuleTest {
    @Test
    fun `ブレーキ摩耗調査用Repositoryがシングルトンとして解決できる`() {
        val koin = koinApplication { modules(lmuWindowsRestApiDataModule) }.koin

        val first = koin.get<LmuWindowsBrakeWearInvestigationRepository>()
        val second = koin.get<LmuWindowsBrakeWearInvestigationRepository>()

        assertSame(first, second)
        koin.close()
    }
}
