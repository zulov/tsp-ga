package pl.zulov.algo

import pl.zulov.data.PathResult
import pl.zulov.data.PointRepository
import java.text.DecimalFormat
import java.util.stream.Stream
import kotlin.random.Random
import kotlin.system.measureTimeMillis

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
            val stepTime = measureTimeMillis{
                parents = sortKill(children)

                children = crossOverAndMutateAndScore(parents)
            }

            logProgress(i, parents, stepTime)
        }

        return parents.first()
    }

    private fun logProgress(i: Int, parents: List<PathResult>, stepTime: Long) {
        if ((i + 1) % 100 == 0) {
            val f = parents.first().result
            val l = parents.last().result
            println(
                "Progress: ${decimalFormat.format((i + 1) / (stepsNo / 100.0))}%, " +
                        "best: $f, " +
                        "range: ${decimalFormat.format(l / f.toFloat() * 100)}% " +
                        "time: ${stepTime}ms"
            )
        }
    }

    private fun crossOverAndMutateAndScore(parents: List<PathResult>): List<PathResult> =
        Stream.concat(
            parents.stream().limit(populationSize - childrenToParentsSize),
            crossoverService.crossover(parents, childrenToParentsSize)
                .map { mutate(it) }
                .map { PathResult(it, score(it)) }
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
        List(populationSize) {
            val path = points.copyOf().also { it.shuffle() }
            PathResult(path, score(path))
        }

    private fun sortKill(
        children: List<PathResult>,
    ): List<PathResult> = children
        .parallelStream()
        .sorted { a, b -> a.result.compareTo(b.result) }
        .limit(survivorNumber)
        .toList()

    private fun score(path: Path): Int {
        var totalCost = 0
        var from = path.first()
        for (to in path) {
            totalCost += costService.getCost(from, to)
            from = to
        }

        totalCost += costService.getCost(path.last(), path.first())
        return totalCost
    }

}