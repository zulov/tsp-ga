package pl.zulov.algo

class TwoOpt(
    private val costService: CostService,
) {
    var totalCounter = 0

    fun improve(path: Path, limit: Int = 5): Path {
        ++totalCounter
        var c = 0
        var improved = true
        val n = path.size
        while (improved && c < limit) {
            improved = false
            for (i in 1 until n - 1) {
                for (j in i + 1 until n) {
                    val a = path[i - 1]
                    val b = path[i]
                    val c = path[j - 1]
                    val d = path[j % n]
                    if (0 < costService.getCostDelta(a, b, c, d)) {
                        path.reverse(i, j)
                        improved = true
                    }
                }
            }
            c++
        }

        return path
    }
}