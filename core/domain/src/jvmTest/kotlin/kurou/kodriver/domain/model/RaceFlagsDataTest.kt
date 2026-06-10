package kurou.kodriver.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class RaceFlagsDataTest {

    // -------------------------------------------------------------------------
    // SessionPhase.fromRaw
    // -------------------------------------------------------------------------

    @Test
    fun `SessionPhase fromRaw - 既知の全値が正しくマッピングされる`() {
        assertEquals(SessionPhase.GARAGE, SessionPhase.fromRaw(0))
        assertEquals(SessionPhase.WARM_UP, SessionPhase.fromRaw(1))
        assertEquals(SessionPhase.GRID_WALK, SessionPhase.fromRaw(2))
        assertEquals(SessionPhase.FORMATION, SessionPhase.fromRaw(3))
        assertEquals(SessionPhase.COUNTDOWN, SessionPhase.fromRaw(4))
        assertEquals(SessionPhase.GREEN_FLAG, SessionPhase.fromRaw(5))
        assertEquals(SessionPhase.FULL_COURSE_YELLOW, SessionPhase.fromRaw(6))
        assertEquals(SessionPhase.SESSION_STOPPED, SessionPhase.fromRaw(7))
        assertEquals(SessionPhase.SESSION_OVER, SessionPhase.fromRaw(8))
        assertEquals(SessionPhase.PAUSED_OR_HEARTBEAT, SessionPhase.fromRaw(9))
    }

    @Test
    fun `SessionPhase fromRaw - 未知の値は UNKNOWN になる`() {
        assertEquals(SessionPhase.UNKNOWN, SessionPhase.fromRaw(99))
        assertEquals(SessionPhase.UNKNOWN, SessionPhase.fromRaw(-1))
        assertEquals(SessionPhase.UNKNOWN, SessionPhase.fromRaw(Int.MIN_VALUE))
    }

    // -------------------------------------------------------------------------
    // SessionYellowFlagState.fromRaw
    // -------------------------------------------------------------------------

    @Test
    fun `SessionYellowFlagState fromRaw - 既知の全値が正しくマッピングされる`() {
        assertEquals(SessionYellowFlagState.INVALID, SessionYellowFlagState.fromRaw(-1))
        assertEquals(SessionYellowFlagState.NONE, SessionYellowFlagState.fromRaw(0))
        assertEquals(SessionYellowFlagState.PENDING, SessionYellowFlagState.fromRaw(1))
        assertEquals(SessionYellowFlagState.PIT_CLOSED, SessionYellowFlagState.fromRaw(2))
        assertEquals(SessionYellowFlagState.PIT_LEAD_LAP, SessionYellowFlagState.fromRaw(3))
        assertEquals(SessionYellowFlagState.PIT_OPEN, SessionYellowFlagState.fromRaw(4))
        assertEquals(SessionYellowFlagState.LAST_LAP, SessionYellowFlagState.fromRaw(5))
        assertEquals(SessionYellowFlagState.RESUME, SessionYellowFlagState.fromRaw(6))
        assertEquals(SessionYellowFlagState.RACE_HALT, SessionYellowFlagState.fromRaw(7))
    }

    @Test
    fun `SessionYellowFlagState fromRaw - 未知の値は UNKNOWN になる`() {
        assertEquals(SessionYellowFlagState.UNKNOWN, SessionYellowFlagState.fromRaw(99))
        assertEquals(SessionYellowFlagState.UNKNOWN, SessionYellowFlagState.fromRaw(-2))
        assertEquals(SessionYellowFlagState.UNKNOWN, SessionYellowFlagState.fromRaw(Int.MIN_VALUE))
    }

    // -------------------------------------------------------------------------
    // SectorFlagState.fromRaw
    // -------------------------------------------------------------------------

    @Test
    fun `SectorFlagState fromRaw - 既知の全値が正しくマッピングされる`() {
        assertEquals(SectorFlagState.CLEAR, SectorFlagState.fromRaw(0))
        assertEquals(SectorFlagState.YELLOW, SectorFlagState.fromRaw(1))
    }

    @Test
    fun `SectorFlagState fromRaw - 未知の値は UNKNOWN になる`() {
        assertEquals(SectorFlagState.UNKNOWN, SectorFlagState.fromRaw(2))
        assertEquals(SectorFlagState.UNKNOWN, SectorFlagState.fromRaw(-1))
        assertEquals(SectorFlagState.UNKNOWN, SectorFlagState.fromRaw(Int.MIN_VALUE))
    }

    // -------------------------------------------------------------------------
    // PrimaryFlag.fromRaw
    // -------------------------------------------------------------------------

    @Test
    fun `PrimaryFlag fromRaw - 既知の全値が正しくマッピングされる`() {
        assertEquals(PrimaryFlag.GREEN, PrimaryFlag.fromRaw(0))
        assertEquals(PrimaryFlag.BLUE, PrimaryFlag.fromRaw(6))
    }

    @Test
    fun `PrimaryFlag fromRaw - 未知の値は UNKNOWN になる`() {
        assertEquals(PrimaryFlag.UNKNOWN, PrimaryFlag.fromRaw(1))
        assertEquals(PrimaryFlag.UNKNOWN, PrimaryFlag.fromRaw(99))
        assertEquals(PrimaryFlag.UNKNOWN, PrimaryFlag.fromRaw(Int.MIN_VALUE))
    }

    // -------------------------------------------------------------------------
    // CountLapFlag.fromRaw
    // -------------------------------------------------------------------------

    @Test
    fun `CountLapFlag fromRaw - 既知の全値が正しくマッピングされる`() {
        assertEquals(CountLapFlag.DO_NOT_COUNT_LAP_OR_TIME, CountLapFlag.fromRaw(0))
        assertEquals(CountLapFlag.COUNT_LAP_BUT_NOT_TIME, CountLapFlag.fromRaw(1))
        assertEquals(CountLapFlag.COUNT_LAP_AND_TIME, CountLapFlag.fromRaw(2))
    }

    @Test
    fun `CountLapFlag fromRaw - 未知の値は UNKNOWN になる`() {
        assertEquals(CountLapFlag.UNKNOWN, CountLapFlag.fromRaw(3))
        assertEquals(CountLapFlag.UNKNOWN, CountLapFlag.fromRaw(-1))
        assertEquals(CountLapFlag.UNKNOWN, CountLapFlag.fromRaw(Int.MIN_VALUE))
    }
}
