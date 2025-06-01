package pl.zulov.algo

import java.util.stream.IntStream.*
import java.util.stream.Stream
import kotlin.random.Random

class InitialPopulationCreator(
    private val costService: CostService,
    private val twoOpt: TwoOpt,
) {
    private var n: Int = 0
    private lateinit var visitedPool: Array<BooleanArray>

    fun create(points: Path, init2optRateSize: Int, initNNRateSize: Int, initRandomRateSize: Int): List<Path> {
        visitedPool = Array(initNNRateSize) { BooleanArray(points.size) { false } }
        n = points.size
        return Stream.concat(
            Stream.concat(
                range(0, init2optRateSize).parallel().mapToObj { twoOpt.improve(points.copyOf().also { it.shuffle() }) },
                range(0, initNNRateSize).parallel().mapToObj { nnPath(it, points) }),
            range(0, initRandomRateSize).parallel().mapToObj { points.copyOf().also { it.shuffle() } }
        )
            .parallel()
            .toList()
    }

    private fun nnPath(i: Int, points: Path): Path {
        val visited = visitedPool[i]
        // visited.fill(false)
        val path = Path(n)
        var currentIdx = Random.nextInt(n)
        path[0] = points[currentIdx]
        visited[currentIdx] = true

        for (i in 1 until n) {
            var nearestIdx = -1
            var minDist = Id.MAX_VALUE
            val currentPoint = points[currentIdx]
            for (j in 0 until n) {
                if (!visited[j]) {
                    val dist = costService.getCost(currentPoint, points[j])
                    if (dist < minDist) {
                        minDist = dist
                        nearestIdx = j
                    }
                }
            }
            currentIdx = nearestIdx
            path[i] = points[currentIdx]
            visited[currentIdx] = true
        }
        return path
    }

}