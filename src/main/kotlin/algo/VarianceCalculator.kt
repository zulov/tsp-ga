package pl.zulov.algo

import pl.zulov.data.PathResult
import kotlin.collections.forEach

class VarianceCalculator {

    //TODO: calculate variance of parents paths
    fun calculateVariance(parents: List<PathResult>): Float {
        val counter = IntArray(parents.first().path.size) { 0 }
        (0 .. counter.size).forEach { i ->
            counter.fill(0)
            parents.forEach { parent ->
                counter[parent.path[i].toInt()]++
            }
            val grouped = counter
                .mapIndexed { index, count -> index to count }
                .groupBy { it.second }
                .entries
                .sortedByDescending { it.key }
        }
        //policzyc czestosc wystepowania danej liczby posortowac od najczescoek wystepujacych
        return 0.0F
    }
}