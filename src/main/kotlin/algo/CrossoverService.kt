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
        val pivot = Random.nextInt(parent1.size)
        val result = IntArray(parent1.size)
        var index = 0

        for (i in 0 until pivot) {
            val element = parent1[i]
            result[index++] = element
            set[element] = true
        }

        for (element in parent2) {
            if (!set[element]) {
                result[index++] = element
            } else {
                set[element] = false
            }
        }
        return result
    }

    fun init(size: Int) {
        set = BooleanArray(size) { false }
    }

}