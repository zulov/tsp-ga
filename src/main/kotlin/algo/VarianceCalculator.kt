package pl.zulov.algo

import pl.zulov.data.PathResult
import kotlin.math.ln

class VarianceCalculator {

    /**
     * Calculates population diversity as mean normalized Shannon entropy across all path positions.
     *
     * For each position in the tour, counts how often each city appears at that position
     * across all parents, then computes the normalized entropy of that frequency distribution.
     *
     * Returns a value in [0, 1]:
     *  - 0.0 means fully converged (every parent has the same city at every position)
     *  - 1.0 means maximum diversity (cities uniformly distributed at every position)
     */
    fun calculateVariance(parents: List<PathResult>): Float {
        if (parents.size <= 1) return 0.0f

        val pathSize = parents.first().path.size
        val populationSize = parents.size
        val maxCityId = parents.maxOf { it.path.max() }.toInt()
        val counter = IntArray(maxCityId + 1)
        val logPopulation = ln(populationSize.toDouble())
        var totalEntropy = 0.0

        for (position in 0 until pathSize) {
            counter.fill(0)

            for (parent in parents) {
                counter[parent.path[position].toInt()]++
            }

            // Shannon entropy for this position
            var entropy = 0.0
            for (count in counter) {
                if (count > 0) {
                    val p = count.toDouble() / populationSize
                    entropy -= p * ln(p)
                }
            }

            // Normalize by max possible entropy (uniform distribution over populationSize distinct cities,
            // but capped by ln(populationSize) since we can have at most populationSize distinct values)
            totalEntropy += entropy / logPopulation
        }

        return (totalEntropy / pathSize).toFloat()
    }
}
