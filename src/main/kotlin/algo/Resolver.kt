package pl.zulov.algo

import pl.zulov.data.PathResult
import pl.zulov.data.PointRepository
import java.text.DecimalFormat
import java.util.stream.Stream
import kotlin.random.Random

const val STEPS_NO = 300
const val POPULATION_SIZE = 100_000
const val SURVIVOR_RATE = 0.8
const val MUTATION_CHANCE = 0.1
const val SURVIVOR_NUMBER = (POPULATION_SIZE * SURVIVOR_RATE).toLong()
const val CHILDREN_TO_PARENTS_SIZE = (POPULATION_SIZE * 0.9).toLong()

typealias Path = ShortArray
typealias Id = Short

class Resolver(
    val pointRepository: PointRepository
) {
    private val crossoverService = CrossoverService()
    private val costService = CostService()
    private val decimalFormat = DecimalFormat("00.0")

    fun process(): PathResult {
        val points = pointRepository.getIds()
        costService.init(pointRepository.getPoints())

        var children = createInitialPopulation(points)
        var parents: List<Path> = emptyList()
        for (i in 0 until STEPS_NO) {
            parents = rateSortKill(children)

            children = crossOverAndMutate(parents)

            logProgress(i, parents)
        }

        return PathResult(score(parents.first()), parents.first())
    }

    private fun logProgress(i: Int, parents: List<Path>) {
        if ((i + 1) % 100 == 0) {
            println(
                "Progress: ${decimalFormat.format((i + 1) / (STEPS_NO / 100.0))}%, " +
                        "best: ${score(parents.first())}, " +
                        "worst: ${score(parents.last())}"
            )
        }
    }

    private fun crossOverAndMutate(parents: List<Path>): List<Path> =
        Stream.concat(
            crossoverService.crossover(parents, CHILDREN_TO_PARENTS_SIZE).map { mutate(it) },
            parents.stream().limit(POPULATION_SIZE - CHILDREN_TO_PARENTS_SIZE)
        ).toList()

    private fun mutate(path: Path): Path =
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

    private fun createInitialPopulation(points: Path): List<Path> =
        List(POPULATION_SIZE) { points.copyOf().also { it.shuffle() } }

    private fun rateSortKill(
        children: List<Path>,
    ): List<Path> = children.parallelStream()
        .map { PathResult(score(it), it) }
        .sorted { a, b -> a.result.compareTo(b.result) }
        .limit(SURVIVOR_NUMBER)
        .map { it.path }
        .toList()

    private fun score(path: Path): Int {
        var totalCost = 0
        var from = path.first()
        for (to in path) {
            totalCost += costService.getCost(from, to)
            from = to
        }

        totalCost += costService.getCost(path.last(), path.first()) // Closing the loop
        return totalCost
    }

}