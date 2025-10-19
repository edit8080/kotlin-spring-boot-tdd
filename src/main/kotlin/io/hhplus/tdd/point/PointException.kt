package io.hhplus.tdd.point

sealed class PointException(message: String) : RuntimeException(message) {
    /** 유효하지 않은 포인트 (0, 음수) */
    class InvalidPointAmount(val amount: Long) :
        PointException("유효하지 않은 포인트 값입니다: $amount. 포인트는 양수여야 합니다.")

    /** 사용할 포인트 부족 (잔존 < 사용량) */
    class InsufficientPoints(val requiredPoints: Long, val currentPoints: Long) :
        PointException("포인트가 부족합니다. 필요: $requiredPoints, 현재: $currentPoints")
}
