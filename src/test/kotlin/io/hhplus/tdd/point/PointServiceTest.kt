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
    fun `0 포인트를 충전하려고 하면 에러가 발생해야 한다`() {
        // given
        val userId = 1L
        val chargeAmount = 0L

        // when & then
        val exception = assertThrows<PointException.InvalidPointAmount> {
            pointService.chargePoint(userId, chargeAmount)
        }
        assertEquals("유효하지 않은 포인트 값입니다: 0. 포인트는 양수여야 합니다.", exception.message)
    }
    
    @Test
    fun `-500 포인트와 같이 음수값으로 충전하려고하면 에러가 발생해야한다`(){
        // given
        val userId = 1L
        val chargeAmount = -500L

        // when
        val exception = assertThrows<PointException.InvalidPointAmount>{ pointService.chargePoint(userId, chargeAmount) }

        // then
        assertEquals(exception.message, "유효하지 않은 포인트 값입니다: -500. 포인트는 양수여야 합니다.")
    }

    @Test
    fun `1000 포인트가 있는 사용자가 1000 포인트를 사용하면 잔액이 0 포인트가 되어야한다`(){
        // given
        val userId = 1L
        userPointTable.insertOrUpdate(userId, 1000L)  // 초기 포인트 설정
        val useAmount = 1000L

        // when
        val result = pointService.usePoint(userId, useAmount)

        // then
        assertEquals(0, result.point)
        assertEquals(userId, result.id)
    }

    @Test
    fun `이미 1000 포인트가 있는 사용자가 1500 포인트를 사용하려고 한다면, 에러가 발생해야한다`(){
        // given
        val userId = 1L
        val userPoint = 1000L
        userPointTable.insertOrUpdate(userId, userPoint)  // 초기 포인트 설정
        val useAmount = 1500L

        // when
        val exception = assertThrows<PointException.InsufficientPoints>{ pointService.usePoint(userId, useAmount) }

        // then
        assertEquals(exception.message, "포인트가 부족합니다. 필요: 1500, 현재: 1000")
    }

    @Test
    fun `0 포인트를 사용하려고 하면 에러가 발생해야 한다`() {
        // given
        val userId = 1L
        userPointTable.insertOrUpdate(userId, 1000L)
        val useAmount = 0L

        // when & then
        val exception = assertThrows<PointException.InvalidPointAmount> {
            pointService.usePoint(userId, useAmount)
        }
        assertEquals("유효하지 않은 포인트 값입니다: 0. 포인트는 양수여야 합니다.", exception.message)
    }

    @Test
    fun `사용하려는 포인트가 -500 같이 음수값이라면 에러가 발생해야한다`(){
        // given
        val userId = 1L
        val useAmount = -500L

        // when
        val exception = assertThrows<PointException.InvalidPointAmount>{ pointService.usePoint(userId, useAmount) }

        // then
        assertEquals(exception.message, "유효하지 않은 포인트 값입니다: -500. 포인트는 양수여야 합니다.")
    }

    @Test
    fun `사용자의 남아있는 포인트를 조회할 수 있어야한다`(){
        // given
        val userId = 1L
        val userPoint = 1000L
        userPointTable.insertOrUpdate(userId, userPoint)

        // when
        val result = pointService.getPoint(userId)

        // then
        assertEquals(userId, result.id)
        assertEquals(userPoint, result.point)
    }

    @Test
    fun `관련된 사용자가 없다면 포인트 조회 시 0 포인트가 조회되어야한다`(){
        // given
        val userId = 1L

        // when
        val result = pointService.getPoint(userId)

        // then
        assertEquals(userId, result.id)
        assertEquals(0, result.point)
    }

    @Test
    fun `포인트를 충전했을 때 충전 이력이 남아있어야한다`(){
        val userId = 1L
        val chargePoint = 1000L

        pointService.chargePoint(userId, chargePoint)

        val histories = pointService.getPointHistories(userId)

        assertEquals(1, histories.size)

        val chargeHistory = histories.first()
        assertEquals(userId, chargeHistory.userId)
        assertEquals(TransactionType.CHARGE, chargeHistory.type)
        assertEquals(chargePoint, chargeHistory.amount)
    }

    @Test
    fun `포인트를 사용했을 때 사용 이력이 남아있어야한다`(){
        val userId = 1L
        val userPoint = 1000L
        userPointTable.insertOrUpdate(userId, userPoint)

        val usePoint = 1000L

        pointService.usePoint(userId, usePoint)

        val histories = pointService.getPointHistories(userId)

        assertEquals(1, histories.size)

        val useHistory = histories.first()
        assertEquals(userId, useHistory.userId)
        assertEquals(TransactionType.USE, useHistory.type)
        assertEquals(usePoint, useHistory.amount)
    }

    @Test
    fun `포인트를 사용한 이력을 사용한 순에 따라 조회할 수 있어야한다`() {
        val userId = 1L
        val chargePoint = 1000L
        val usePoint = 500L

        // 포인트 충전 후 포인트 사용
        pointService.chargePoint(userId, chargePoint)
        pointService.usePoint(userId, usePoint)

        val histories = pointService.getPointHistories(userId)

        assertEquals(2, histories.size)

        assertEquals(TransactionType.CHARGE, histories[0].type)
        assertEquals(TransactionType.USE, histories[1].type)
    }

    @Test
    fun `포인트 충전,사용 시 에러가 발생하면 이력 목록에 남아선 안된다`() {
        val userId = 1L
        val chargePoint = 1000L
        val usePoint = 1500L

        // 포인트 충전 후 포인트 사용
        // - 사용 시 1000 포인트보다 더 많은 1500 포인트를 사용하여 에러 발생
        pointService.chargePoint(userId, chargePoint)
        assertThrows<PointException.InsufficientPoints>{ pointService.usePoint(userId, usePoint) }

        val histories = pointService.getPointHistories(userId)

        assertEquals(1, histories.size)
        assertEquals(TransactionType.CHARGE, histories[0].type)
    }
}
