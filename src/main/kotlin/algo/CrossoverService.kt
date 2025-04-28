package pl.zulov.algo

import kotlin.random.Random

class CrossoverService {

    private lateinit var set: BooleanArray

    fun crossover(parents: List<IntArray>, populationSize: Int): List<IntArray> =
        (0 until populationSize)
            .map { crossover(parents.random(), parents.random()) }

    fun crossover(
        parent1: IntArray,
        parent2: IntArray,
    ): IntArray {
        val pivot = Random.nextInt(0, parent1.size)
        val result = IntArray(parent1.size)
        System.arraycopy(parent1, 0, result, 0, pivot)

        for (i in 0 until pivot) {
            set[parent1[i]] = true
        }
        var index = pivot

        for (id in parent2) {
            if (set[id]) {
                set[id] = false
            } else {
                result[index++] = id
            }
        }
        return result
    }

    fun init(size: Int) {
        set = BooleanArray(size) { false }
    }

}