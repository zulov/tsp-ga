package pl.zulov.algo

import kotlin.random.Random

class InitialPopulationCreator(
    private val costService: CostService
) {
    private var n: Int = 0
    private lateinit var visited: BooleanArray

    fun create(points: Path, init2optRateSize: Int, initNNRateSize: Int, initRandomRateSize: Int): List<Path> {
        visited = BooleanArray(points.size)
        n = points.size
        return List(init2optRateSize) { twoOpt(points) } +
                List(initNNRateSize) { nnPath(points) } +
                List(initRandomRateSize) { points.copyOf().also { it.shuffle() } }
    }

    private fun nnPath(points: Path): Path {
        visited.fill(false)
        val path = Path(n)
        var currentIdx = Random.nextInt(n)
        path[0] = points[currentIdx]
        visited[currentIdx] = true

        for (i in 1 until n) {
            var nearestIdx = -1
            var minDist = Id.MAX_VALUE
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

    private fun twoOpt(path: Path): Path {
        var improved = true
        val result = path.copyOf().also { it.shuffle() }
        while (improved) {
            improved = false
            for (i in 1 until n - 1) {// zapisac jako jedeą listę par
                for (j in i + 1 until n) {//tu chyba jest błąd, powinno być j <= n moze wyzej tez?
                    val a = result[i - 1]
                    val b = result[i]
                    val c = result[j - 1]
                    val d = result[j % n]
                    val currentCost = costService.getCost(a, b) + costService.getCost(c, d)
                    val newCost = costService.getCost(a, c) + costService.getCost(b, d)
                    if (newCost < currentCost) {
                        result.reverse(i, j)
                        improved = true
                    }
                }
            }
        }
        return result
    }

}