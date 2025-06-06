package pl.zulov.algo

class TwoOpt(
    private val costService: CostService,
) {
    var totalCounter = 0

    // ocenianie poprawy tylko na fragmencie ma sens jeżeli koszt w obie strony jest taki sam
    // zrobic wersje że zamienniamy tylko najlepsze zyski z iteracji po j
    fun improve(path: Path, limit: Int = 5): Path {
        ++totalCounter
        var c = 0
        var improved = true
        val n = path.size
        while (improved && c < limit) {
            improved = false
            for (i in 1 until n - 1) {
                val a = path[i - 1]
                for (j in i + 2 until n) {
                    val b = path[i]
                    val c = path[j - 1]
                    val d = path[j]
                    if (costService.deltaBelowZero(a, b, c, d)) {
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