package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kurou.kodriver.domain.repository.AccessLocalNetworkPermissionRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckAccessLocalNetworkPermissionGrantedUseCaseTest {
    @MockK
    private lateinit var repository: AccessLocalNetworkPermissionRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `Repositoryがtrueを返す場合trueを返す`() {
        every { repository.isGranted() } returns true

        assertTrue(CheckAccessLocalNetworkPermissionGrantedUseCase(repository)())
        verify(exactly = 1) { repository.isGranted() }
        confirmVerified(repository)
    }

    @Test
    fun `Repositoryがfalseを返す場合falseを返す`() {
        every { repository.isGranted() } returns false

        assertFalse(CheckAccessLocalNetworkPermissionGrantedUseCase(repository)())
        verify(exactly = 1) { repository.isGranted() }
        confirmVerified(repository)
    }
}
