package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
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

    @Test
    fun `포인트 사용 시 잔액이 부족하면 예외가 발생하고 insertOrUpdate는 호출되지 않아야 한다`() {
        // given
        val userId = 1L
        val currentPoint = 1000L
        val useAmount = 1500L

        // Mock 설정: selectById 호출 시 1000포인트 반환
        whenever(userPointTable.selectById(userId))
            .thenReturn(UserPoint(userId, currentPoint, System.currentTimeMillis()))

        // when & then - 예외 발생
        val exception = assertThrows<PointException.InsufficientPoints> {
            pointService.usePoint(userId, useAmount)
        }

        assertEquals("포인트가 부족합니다. 필요: 1500, 현재: 1000", exception.message)

        // then - selectById는 호출되었지만, insertOrUpdate와 insert는 호출되지 않아야 함
        verify(userPointTable).selectById(userId)
        verify(userPointTable, org.mockito.kotlin.never()).insertOrUpdate(any(), any())
        verify(pointHistoryTable, org.mockito.kotlin.never()).insert(any(), any(), any(), any())
    }

    // ========== 포인트 계산 로직 검증 ==========

    @Test
    fun `포인트 충전 시 현재 포인트에 충전 금액이 더해져서 insertOrUpdate가 호출되어야 한다`() {
        // given
        val userId = 1L
        val currentPoint = 1000L
        val chargeAmount = 500L
        val expectedPoint = 1500L

        // Mock 설정: selectById - 현재 포인트 반환
        whenever(userPointTable.selectById(userId))
            .thenReturn(UserPoint(userId, currentPoint, System.currentTimeMillis()))

        // Mock 설정: insertOrUpdate - Service가 사용할 반환값 제공
        whenever(userPointTable.insertOrUpdate(any(), any()))
            .thenReturn(UserPoint(userId, expectedPoint, System.currentTimeMillis()))

        // when
        pointService.chargePoint(userId, chargeAmount)

        // then - 포인트 계산 로직만 검증
        verify(userPointTable).selectById(userId)
        verify(userPointTable).insertOrUpdate(userId, expectedPoint)  // 1000 + 500 = 1500
    }

    @Test
    fun `포인트 사용 시 현재 포인트에서 사용 금액이 빠져서 insertOrUpdate가 호출되어야 한다`() {
        // given
        val userId = 1L
        val currentPoint = 1000L
        val useAmount = 300L
        val expectedPoint = 700L

        // Mock 설정: selectById - 현재 포인트 반환
        whenever(userPointTable.selectById(userId))
            .thenReturn(UserPoint(userId, currentPoint, System.currentTimeMillis()))

        // Mock 설정: insertOrUpdate - Service가 사용할 반환값 제공
        whenever(userPointTable.insertOrUpdate(any(), any()))
            .thenReturn(UserPoint(userId, expectedPoint, System.currentTimeMillis()))

        // when
        pointService.usePoint(userId, useAmount)

        // then - 포인트 계산 로직만 검증
        verify(userPointTable).selectById(userId)
        verify(userPointTable).insertOrUpdate(userId, expectedPoint)  // 1000 - 300 = 700
    }

    // ========== 이력 저장 검증 ==========

    @Test
    fun `포인트 충전 시 충전 이력이 저장되어야 한다`() {
        // given
        val userId = 1L
        val currentPoint = 1000L
        val chargeAmount = 500L
        val timestamp = System.currentTimeMillis()

        // Mock 설정: Service가 정상 동작하도록
        whenever(userPointTable.selectById(any()))
            .thenReturn(UserPoint(userId, currentPoint, timestamp))
        whenever(userPointTable.insertOrUpdate(any(), any()))
            .thenReturn(UserPoint(userId, 1500L, timestamp))

        // when
        pointService.chargePoint(userId, chargeAmount)

        // then - 이력 저장 검증
        verify(pointHistoryTable).insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), any())
    }

    @Test
    fun `포인트 사용 시 사용 이력이 저장되어야 한다`() {
        // given
        val userId = 1L
        val currentPoint = 1000L
        val useAmount = 300L
        val timestamp = System.currentTimeMillis()

        // Mock 설정: Service가 정상 동작하도록
        whenever(userPointTable.selectById(any()))
            .thenReturn(UserPoint(userId, currentPoint, timestamp))
        whenever(userPointTable.insertOrUpdate(any(), any()))
            .thenReturn(UserPoint(userId, 700L, timestamp))

        // when
        pointService.usePoint(userId, useAmount)

        // then - 이력 저장 검증
        verify(pointHistoryTable).insert(eq(userId), eq(useAmount), eq(TransactionType.USE), eq(timestamp))
    }
}
