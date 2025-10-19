package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable

class PointService(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable
) {
    // 포인트 충전
    fun chargePoint(userId: Long, amount: Long): UserPoint {
        if(amount <= 0){
            throw PointException.InvalidPointAmount(amount);
        }

        val userPoint = userPointTable.selectById(userId)
        val updatedPointAmount = userPoint.point + amount;
        val updatedUserPoint = userPointTable.insertOrUpdate(userId, updatedPointAmount)

        return updatedUserPoint
    }

    // 포인트 사용
    fun usePoint(userId: Long, amount: Long): UserPoint {
        if(amount <= 0){
            throw PointException.InvalidPointAmount(amount);
        }

        val userPoint = userPointTable.selectById(userId)
        val updatedPointAmount = userPoint.point - amount

        if(updatedPointAmount <= 0){
            throw PointException.InsufficientPoints(amount, userPoint.point);
        }

        val updatedUserPoint = userPointTable.insertOrUpdate(userId, updatedPointAmount)
        return updatedUserPoint;
    }

    // 포인트 조회
    fun getPoint(userId: Long): UserPoint {
        return userPointTable.selectById(userId);
    }
}
