package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals

internal inline fun <reified R : Any, T> assertObserves(
    useCaseName: String,
    expected: T,
    crossinline observe: R.() -> Flow<T>,
    crossinline useCase: (R) -> Flow<T>,
) {
    val repository = mockk<R>()
    every { repository.observe() } returns flowOf(expected)
    try {
        runTest {
            assertEquals(expected, useCase(repository).first())
        }
        verify(exactly = 1) { repository.observe() }
        confirmVerified(repository)
    } catch (e: AssertionError) {
        throw AssertionError("$useCaseName: ${e.message}", e)
    }
}

internal inline fun <reified R : Any> assertSaves(
    useCaseName: String,
    crossinline save: suspend R.() -> Unit,
    crossinline useCase: suspend (R) -> Unit,
) {
    val repository = mockk<R>(relaxUnitFun = true)
    try {
        runTest {
            useCase(repository)
        }
        coVerify(exactly = 1) { repository.save() }
        confirmVerified(repository)
    } catch (e: AssertionError) {
        throw AssertionError("$useCaseName: ${e.message}", e)
    }
}
