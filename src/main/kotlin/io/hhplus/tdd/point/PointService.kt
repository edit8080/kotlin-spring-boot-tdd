package io.hhplus.tdd.point

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable

class PointService(
    private val userPointTable: UserPointTable,
    private val pointHistoryTable: PointHistoryTable
) {

    fun chargePoint(userId: Long, amount: Long): UserPoint {
        return UserPoint(
            id = userId,
            point = amount,
            updateMillis = System.currentTimeMillis(),
        )
    }
}
