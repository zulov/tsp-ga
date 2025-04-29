package pl.zulov.algo

import pl.zulov.data.PathResult
import pl.zulov.data.PointRepository
import java.text.DecimalFormat
import java.util.stream.Stream
import kotlin.random.Random

const val STEPS_NO = 20000
const val POPULATION_SIZE = 100000
const val SURVIVOR_RATE = 0.8
const val MUTATION_CHANCE = 0.1
const val SURVIVOR_NUMBER = (POPULATION_SIZE * SURVIVOR_RATE).toLong()
const val CHILDREN_TO_PARENTS_SIZE = (POPULATION_SIZE * 0.9).toLong()

class Resolver(
    val pointRepository: PointRepository
) {
    private val crossoverService = CrossoverService()
    private val costService = CostService()
    private val decimalFormat = DecimalFormat("000.0")

    fun process(): PathResult {
        val points = pointRepository.getPoints().map { it.id }.toIntArray()
        costService.init(pointRepository.getPoints())

        var children = createInitialPopulation(points)
        var parents: List<PathResult> = emptyList()
        for (i in 0 until STEPS_NO) {
            parents = rateSortKill(children)

            children = crossOverAndMutate(parents.map { it.path })

            if ((i + 1) % 100 == 0) {
                println(
                    "Progress: ${decimalFormat.format((i + 1) / (STEPS_NO / 100.0))}%, " +
                            "best: ${parents.first().result}, " +
                            "worst: ${parents.last().result}"
                )
            }
        }

        return parents.first()
    }

    private fun crossOverAndMutate(parents: List<IntArray>): List<IntArray> =
        Stream.concat(
            crossoverService.crossover(parents, CHILDREN_TO_PARENTS_SIZE).map { mutate(it) },
            parents.stream().limit(POPULATION_SIZE - CHILDREN_TO_PARENTS_SIZE)
        ).toList()

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

    private fun createInitialPopulation(points: IntArray): List<IntArray> =
        List(POPULATION_SIZE) { points.copyOf().also { it.shuffle() } }

    private fun rateSortKill(
        children: List<IntArray>,
    ): List<PathResult> = children.parallelStream()
        .map { PathResult(score(it), it) }
        .sorted { a, b -> a.result.compareTo(b.result) }
        .limit(SURVIVOR_NUMBER)
        .toList()

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