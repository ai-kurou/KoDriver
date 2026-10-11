package kurou.kodriver.presentation

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class DebugStateDetailTypeTest(
    private val simulatorId: String,
    private val expected: DebugStateDetailType,
) {
    @Test
    fun `選択中のシミュレーターIDに対応する詳細ペインを返す`() {
        assertEquals(expected, debugStateDetailType(simulatorId))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun simulatorIds(): List<Array<Any>> =
            listOf(
                arrayOf("lmu_windows", DebugStateDetailType.LMU_WINDOWS),
                arrayOf("gt7_ps5", DebugStateDetailType.GT7_PS5),
                arrayOf("ace_windows", DebugStateDetailType.ACE_WINDOWS),
                arrayOf("unknown", DebugStateDetailType.UNKNOWN),
            )
    }
}
