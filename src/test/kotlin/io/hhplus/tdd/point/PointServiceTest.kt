package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class PointServiceTest {

    private lateinit var userPointTable: UserPointTable
    private lateinit var pointHistoryTable: PointHistoryTable
    private lateinit var pointService: PointService

    @BeforeEach
    fun setUp() {
        userPointTable = UserPointTable()
        pointHistoryTable = PointHistoryTable()
        pointService = PointService(userPointTable, pointHistoryTable)
    }

    @Test
    fun `사용자에게 100포인트를 충전하면 잔액이 100이 되어야 한다`() {
        // given
        val userId = 1L
        val chargeAmount = 100L

        // when
        val result = pointService.chargePoint(userId, chargeAmount)

        // then
        assertEquals(100L, result.point)
        assertEquals(userId, result.id)
    }

    @Test
    fun `이미 1000 포인트가 있는 사용자에게 500포인트를 충전하면 잔액이 1500이 되어야 한다`() {
        // given
        val userId = 1L
        userPointTable.insertOrUpdate(userId, 1000L)  // 초기 포인트 설정
        val chargeAmount = 500L

        // when
        val result = pointService.chargePoint(userId, chargeAmount)

        // then
        assertEquals(1500L, result.point)
        assertEquals(userId, result.id)
    }

    @Test
    fun `-500 포인트와 같이 음수값으로 충전하려고하면 에러가 발생해야한다`(){
        // given
        val userId = 1L
        val chargeAmount = -500L

        // when
        val exception = assertThrows<PointException.InvalidPointAmount>{ pointService.chargePoint(userId, chargeAmount) }

        // then
        assertEquals(exception.message, "유효하지 않은 포인트 값입니다: -500. 포인트는 0 또는 양수여야 합니다.")
    }
}
