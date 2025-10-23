package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PointServiceConcurrencyTest {

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
    fun `동일한 사용자에게 10개의 스레드가 동시에 100포인트씩 충전하면 최종 잔액이 1000포인트가 되어야 한다`() {
        // given
        val userId = 1L
        val threadCount = 10
        val chargeAmountPerThread = 100L
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // when
        repeat(threadCount) {
            executor.submit {
                try {
                    pointService.chargePoint(userId, chargeAmountPerThread)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await(10, TimeUnit.SECONDS)
        executor.shutdown()

        // then
        val result = pointService.getPoint(userId)
        assertEquals(1000L, result.point)
    }

    @Test
    fun `1000포인트를 가진 사용자에게 5개의 스레드가 동시에 100포인트씩 사용하면 최종 잔액이 500포인트가 되어야 한다`() {
        // given
        val userId = 1L
        userPointTable.insertOrUpdate(userId, 1000L)
        val threadCount = 5
        val useAmountPerThread = 100L
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // when
        repeat(threadCount) {
            executor.submit {
                try {
                    pointService.usePoint(userId, useAmountPerThread)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await(10, TimeUnit.SECONDS)
        executor.shutdown()

        // then
        val result = pointService.getPoint(userId)
        assertEquals(500L, result.point)
    }
}
