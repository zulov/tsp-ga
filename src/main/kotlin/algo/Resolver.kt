package pl.zulov.algo

import pl.zulov.data.PathResult
import pl.zulov.data.PointRepository
import java.text.DecimalFormat
import java.util.stream.Stream
import kotlin.random.Random

typealias Path = ShortArray
typealias Id = Short

class Resolver(
    val pointRepository: PointRepository,
    private val stepsNo: Int,
    private val populationSize: Int,
    survivorRate: Float,
    private val mutationChance: Float,
    grandfatherRate: Float,
) {
    private val crossoverService = CrossoverService()
    private val costService = CostService()
    private val decimalFormat = DecimalFormat("00.0")

    private val survivorNumber = (populationSize * survivorRate).toLong()
    private val childrenToParentsSize = (populationSize * grandfatherRate).toLong()

    fun process(): PathResult {
        val points = pointRepository.getIds()
        costService.init(pointRepository.getPoints())

        var children = createInitialPopulation(points)
        var parents: List<Path> = emptyList()
        for (i in 0 until stepsNo) {
            parents = rateSortKill(children)

            children = crossOverAndMutate(parents)

            logProgress(i, parents)
        }

        return PathResult(score(parents.first()), parents.first())
    }

    private fun logProgress(i: Int, parents: List<Path>) {
        if ((i + 1) % 100 == 0) {
            println(
                "Progress: ${decimalFormat.format((i + 1) / (stepsNo / 100.0))}%, " +
                        "best: ${score(parents.first())}, " +
                        "worst: ${score(parents.last())}"
            )
        }
    }

    private fun crossOverAndMutate(parents: List<Path>): List<Path> =
        Stream.concat(
            parents.stream().limit(populationSize - childrenToParentsSize),//zachować oceny
            crossoverService.crossover(parents, childrenToParentsSize).map { mutate(it) }
        ).toList()

    private fun mutate(path: Path): Path =
        if ((Random.nextFloat() < mutationChance)) {
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
        List(populationSize) { points.copyOf().also { it.shuffle() } }

    private fun rateSortKill(
        children: List<Path>,
    ): List<Path> = children.parallelStream()
        .map { PathResult(score(it), it) }
        .sorted { a, b -> a.result.compareTo(b.result) }
        .limit(survivorNumber)
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