package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class PointService(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable
) {
    // 사용자별 락 저장소 선언
    private val userLocks = ConcurrentHashMap<Long, ReentrantLock>()

    // 포인트 충전
    fun chargePoint(userId: Long, amount: Long): UserPoint {
        if(amount <= 0){
            throw PointException.InvalidPointAmount(amount);
        }

        val lock = userLocks.computeIfAbsent(userId) { ReentrantLock() }

        lock.withLock {
            val userPoint = userPointTable.selectById(userId)
            val updatedPointAmount = userPoint.point + amount;
            val updatedUserPoint = userPointTable.insertOrUpdate(userId, updatedPointAmount)

            // 포인트 충전 이력 저장
            pointHistoryTable.insert(
                userId,
                amount,
                TransactionType.CHARGE,
                updatedUserPoint.updateMillis
            )

            return updatedUserPoint
        }
    }

    // 포인트 사용
    fun usePoint(userId: Long, amount: Long): UserPoint {
        if(amount <= 0){
            throw PointException.InvalidPointAmount(amount);
        }

        val userPoint = userPointTable.selectById(userId)
        val updatedPointAmount = userPoint.point - amount

        if(updatedPointAmount < 0){
            throw PointException.InsufficientPoints(amount, userPoint.point);
        }

        val updatedUserPoint = userPointTable.insertOrUpdate(userId, updatedPointAmount)

        // 포인트 사용 이력 저장
        pointHistoryTable.insert(
            userId,
            amount,
            TransactionType.USE,
            updatedUserPoint.updateMillis
        )

        return updatedUserPoint;
    }

    // 포인트 조회
    fun getPoint(userId: Long): UserPoint {
        return userPointTable.selectById(userId);
    }

    // 포인트 이력 조회
    fun getPointHistories(userId: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(userId)
    }
}
