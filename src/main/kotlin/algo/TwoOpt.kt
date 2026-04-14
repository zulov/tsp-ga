package pl.zulov.algo

class TwoOpt(
    private val costService: CostService,
) {

    // ocenianie poprawy tylko na fragmencie ma sens jeżeli koszt w obie strony jest taki sam
    fun improve(path: Path, limit: Int = 5): Path {
        var iteration = 0
        var improved = true
        val n = path.size
        while (improved && iteration < limit) {
            improved = false
            for (i in 0 until n - 1) {
                val a = path[(i - 1 + n) % n]
                for (j in i + 2 until n) {
                    if (i == 0 && j == n - 1) continue
                    val b = path[i]
                    val c = path[j - 1]
                    val d = path[j % n]
                    if (costService.deltaBelowZero(a, b, c, d)) {
                        path.reverse(i, j)
                        improved = true
                    }
                }
            }
            iteration++
        }

        return path
    }
}