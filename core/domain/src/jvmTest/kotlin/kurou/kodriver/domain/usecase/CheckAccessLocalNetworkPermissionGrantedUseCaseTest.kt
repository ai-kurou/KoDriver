package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kurou.kodriver.domain.repository.AccessLocalNetworkPermissionRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckAccessLocalNetworkPermissionGrantedUseCaseTest {
    private val repository: AccessLocalNetworkPermissionRepository = mockk()

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
