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
        crossoverService.init(populationSize, pointRepository.getPoints().size)
        val points = pointRepository.getIds()
        costService.init(pointRepository.getPoints())

        var children = createInitialPopulation(points)
        var parents: List<PathResult> = emptyList()
        for (i in 0 until stepsNo) {
            parents = rateSortKill(children)

            children = crossOverAndMutate(parents)

            logProgress(i, parents)
        }

        return parents.first()
    }

    private fun logProgress(i: Int, parents: List<PathResult>) {
        if ((i + 1) % 100 == 0) {
            println(
                "Progress: ${decimalFormat.format((i + 1) / (stepsNo / 100.0))}%, " +
                        "best: ${parents.first().result}, " +
                        "worst: ${parents.last().result}"
            )
        }
    }

    private fun crossOverAndMutate(parents: List<PathResult>): List<PathResult> =
        Stream.concat(
            parents.stream().limit(populationSize - childrenToParentsSize),
            crossoverService.crossover(parents, childrenToParentsSize).map { mutate(it) }.map { PathResult(it, null) }
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

    private fun createInitialPopulation(points: Path): List<PathResult> =
        List(populationSize) { PathResult(points.copyOf().also { it.shuffle() }, null) }

    private fun rateSortKill(
        children: List<PathResult>,
    ): List<PathResult> = children.parallelStream()
        .map {
            if (it.result == null) {
                it.result = score(it.path)
            }
            it
        }
        .sorted { a, b -> a.result!!.compareTo(b.result!!) }
        .limit(survivorNumber)
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