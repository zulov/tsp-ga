package pl.zulov.algo

import java.util.stream.Stream
import kotlin.random.Random

class CrossoverService {

    fun crossover(parents: List<IntArray>, populationSize: Long): Stream<IntArray> =
        (0 until populationSize).toList().parallelStream()
            .map { crossover(parents.random(), parents.random()) }

    fun crossover(
        parent1: IntArray,
        parent2: IntArray,
    ): IntArray {
        val set = BooleanArray(parent1.size) { false }
        val pivot = Random.nextInt(0, parent1.size)
        val result = IntArray(parent1.size)
        System.arraycopy(parent1, 0, result, 0, pivot)

        for (i in 0 until pivot) {
            set[parent1[i]] = true
        }
        var index = pivot

        for (id in parent2) {
            if (!set[id]) {
                result[index++] = id
            }
        }
        return result
    }

}