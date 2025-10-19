package io.hhplus.tdd.point

sealed class PointException(message: String) : RuntimeException(message) {
    /** 유효하지 않은 포인트 (음수) */
    class InvalidPointAmount(val amount: Long) :
        PointException("유효하지 않은 포인트 값입니다: $amount. 포인트는 0 또는 양수여야 합니다.")
}
