package io.hhplus.tdd.point

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/point")
class PointController(
    private val pointService: PointService
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * 특정 유저의 포인트를 조회
     */
    @GetMapping("{id}")
    fun point(
        @PathVariable id: Long,
    ): UserPoint {
        logger.info("포인트 조회 요청 - userId: {}", id)
        val result = pointService.getPoint(id)
        logger.info("포인트 조회 완료 - userId: {}, point: {}", id, result.point)
        return result
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회
     */
    @GetMapping("{id}/histories")
    fun history(
        @PathVariable id: Long,
    ): List<PointHistory> {
        logger.info("포인트 이력 조회 요청 - userId: {}", id)
        val result = pointService.getPointHistories(id)
        logger.info("포인트 이력 조회 완료 - userId: {}, count: {}", id, result.size)
        return result
    }

    /**
     * 특정 유저의 포인트를 충전
     */
    @PatchMapping("{id}/charge")
    fun charge(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        logger.info("포인트 충전 요청 - userId: {}, amount: {}", id, amount)
        val result = pointService.chargePoint(id, amount)
        logger.info("포인트 충전 완료 - userId: {}, newPoint: {}", id, result.point)
        return result
    }

    /**
     * 특정 유저의 포인트를 사용
     */
    @PatchMapping("{id}/use")
    fun use(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        logger.info("포인트 사용 요청 - userId: {}, amount: {}", id, amount)
        val result = pointService.usePoint(id, amount)
        logger.info("포인트 사용 완료 - userId: {}, remainingPoint: {}", id, result.point)
        return result
    }
}
