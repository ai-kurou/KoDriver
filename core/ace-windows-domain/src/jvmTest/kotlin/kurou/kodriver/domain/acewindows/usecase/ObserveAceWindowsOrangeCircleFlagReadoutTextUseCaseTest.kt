package kurou.kodriver.domain.acewindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAceWindowsOrangeCircleFlagReadoutTextUseCaseTest {
    private val repository: AceWindowsFlagReadoutTextPreferencesRepository = mockk()

    @Test
    fun `Repositoryの値をそのまま流す`() =
        runTest {
            every { repository.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) } returns
                flowOf("オレンジボールフラッグ、車両に不具合があります")

            assertEquals(
                "オレンジボールフラッグ、車両に不具合があります",
                ObserveAceWindowsOrangeCircleFlagReadoutTextUseCase(repository)().first(),
            )
            verify(exactly = 1) { repository.observeText(AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE) }
            confirmVerified(repository)
        }
}
