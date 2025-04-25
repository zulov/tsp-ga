package pl.zulov.algo

import kotlin.random.Random

class CrossoverService {

    private lateinit var set: BooleanArray

    fun crossover(parents: List<List<Int>>, populationSize:Int): List<List<Int>> =
        (0 until populationSize)
            .map { crossover(parents.random(), parents.random()) }

    private fun crossover(
        parent1: List<Int>,
        parent2: List<Int>,
    ): List<Int> {
        val firstHalf = parent1.subList(0, Random.nextInt(parent1.size))
        firstHalf.forEach { set[it] = true }
        val secondHalf = parent2.filter { !set[it] }
        firstHalf.forEach { set[it] = false }
        return firstHalf + secondHalf
    }

    fun init(size: Int) {
        set = BooleanArray(size)
    }

}