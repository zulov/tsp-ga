package pl.zulov.algo

import kotlin.random.Random

class InitialPopulationCreator(
    private val costService: CostService
) {
    private var n: Int = 0
    private lateinit var visited: BooleanArray

    fun create(points: Path, initNNRateSize: Int, initRandomRateSize: Int): List<Path> {
        visited = BooleanArray(points.size)
        n = points.size
        return List(initNNRateSize) { generateNNPath(points) } + List(initRandomRateSize) { points.copyOf().also { it.shuffle() } }
    }

    private fun generateNNPath(points: Path): Path {
        visited.fill(false)
        val path = ShortArray(n)
        var currentIdx = Random.nextInt(n)
        path[0] = points[currentIdx]
        visited[currentIdx] = true

        for (i in 1 until n) {
            var nearestIdx = -1
            var minDist = Short.MAX_VALUE
            for (j in 0 until n) {
                val currentPoint = points[currentIdx]
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