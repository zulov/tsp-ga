package pl.zulov.algo

import pl.zulov.data.PathResult
import pl.zulov.data.PointRepository
import kotlin.random.Random

const val STEPS_NO = 100
const val POPULATION_SIZE = 50000
const val SURVIVOR_RATE = 0.8
const val MUTATION_CHANCE = 0.05
const val SURVIVOR_NUMBER = (POPULATION_SIZE * SURVIVOR_RATE).toInt()
const val CHILDREN_TO_PARENTS_SIZE = (POPULATION_SIZE * 0.9).toInt()

class Resolver(
    val pointRepository: PointRepository
) {
    private val crossoverService = CrossoverService()
    private val costService = CostService()

    fun process(): PathResult {
        val points = pointRepository.getPoints().map { it.id }.toIntArray()
        costService.init(pointRepository.getPoints())
        crossoverService.init(points.size)
        var children = createInitialPopulation(points)
        var parents: List<PathResult> = emptyList()
        for (i in 0 until STEPS_NO) {
            parents = rateSortKill(children)
            val parentsPaths = parents.map { it.path }
            children = crossOverAndMutate(parentsPaths)

            children += parentsPaths.take(POPULATION_SIZE - children.size)
            if ((i + 1) % 100 == 0) {
                println(
                    "Progress: ${(i + 1)}/$STEPS_NO, " +
                            "best: ${parents.first().result}, " +
                            "worst: ${parents.last().result}"
                )
            }
        }

        return parents.first()
    }

    private fun crossOverAndMutate(parents: List<IntArray>): MutableList<IntArray> =
        crossoverService.crossover(parents, CHILDREN_TO_PARENTS_SIZE)
            .map { mutate(it) }
            .toMutableList()

    private fun mutate(path: IntArray): IntArray =
        if ((Random.nextFloat() < MUTATION_CHANCE)) {
            val j = Random.nextInt(path.size)
            val i = Random.nextInt(path.size)

            val a = path[i]
            val b = path[j]
            path[i] = b
            path[j] = a
            path
        } else {
            path
        }

    private fun createInitialPopulation(points: IntArray): MutableList<IntArray> =
        MutableList(POPULATION_SIZE) { points.copyOf().also { it.shuffle() } }

    private fun rateSortKill(
        children: List<IntArray>,
    ): List<PathResult> = children.map { PathResult(score(it), it) }
        .sortedBy { it.result }
        .take(SURVIVOR_NUMBER)

    private fun score(path: IntArray): Int {
        var totalCost = 0
        val iterator = path.iterator()
        var from = iterator.next() // Skip the first element
        while (iterator.hasNext()) {
            val to = iterator.next()
            totalCost += costService.getCost(from, to)
            from = to
        }
        totalCost += costService.getCost(path.last(), path.first()) // Closing the loop
        return totalCost
    }

}