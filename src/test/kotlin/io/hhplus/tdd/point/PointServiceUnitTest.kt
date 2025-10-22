package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.junit.jupiter.api.Assertions.*

class PointServiceUnitTest {

    @Mock
    private lateinit var userPointTable: UserPointTable

    @Mock
    private lateinit var pointHistoryTable: PointHistoryTable

    @InjectMocks
    private lateinit var pointService: PointService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    // ========== 충전(chargePoint) 예외 검증 ==========

    @Test
    fun `포인트 충전 시 음수 금액이면 예외가 발생해야 한다`() {
        // given
        val userId = 1L
        val invalidAmount = -500L

        // when & then
        val exception = assertThrows<PointException.InvalidPointAmount> {
            pointService.chargePoint(userId, invalidAmount)
        }

        assertEquals("유효하지 않은 포인트 값입니다: -500. 포인트는 양수여야 합니다.", exception.message)
    }

    @Test
    fun `포인트 충전 시 0 포인트면 예외가 발생해야 한다`() {
        // given
        val userId = 1L
        val invalidAmount = 0L

        // when & then
        val exception = assertThrows<PointException.InvalidPointAmount> {
            pointService.chargePoint(userId, invalidAmount)
        }

        assertEquals("유효하지 않은 포인트 값입니다: 0. 포인트는 양수여야 합니다.", exception.message)
    }

    // ========== 사용(usePoint) 예외 검증 ==========

    @Test
    fun `포인트 사용 시 음수 금액이면 예외가 발생해야 한다`() {
        // given
        val userId = 1L
        val invalidAmount = -500L

        // when & then
        val exception = assertThrows<PointException.InvalidPointAmount> {
            pointService.usePoint(userId, invalidAmount)
        }

        assertEquals("유효하지 않은 포인트 값입니다: -500. 포인트는 양수여야 합니다.", exception.message)
    }

    @Test
    fun `포인트 사용 시 0 포인트면 예외가 발생해야 한다`() {
        // given
        val userId = 1L
        val invalidAmount = 0L

        // when & then
        val exception = assertThrows<PointException.InvalidPointAmount> {
            pointService.usePoint(userId, invalidAmount)
        }

        assertEquals("유효하지 않은 포인트 값입니다: 0. 포인트는 양수여야 합니다.", exception.message)
    }
}
