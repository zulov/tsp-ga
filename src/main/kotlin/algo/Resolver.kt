package pl.zulov.algo

import pl.zulov.ResultKey
import pl.zulov.data.PathResult
import pl.zulov.data.PointRepository
import java.util.stream.Stream
import kotlin.random.Random
import kotlin.system.measureTimeMillis

typealias Path = ShortArray
typealias Id = Short

class Resolver(
    val pointRepository: PointRepository,
) {
    private val costService = CostService()
    private val arrayProvider = ArrayProvider()
    private val initialPopulationCreator = InitialPopulationCreator(costService)
    private val crossoverService = CrossoverService(arrayProvider)

    private var stepsNo = 0
    private var populationSize = 0
    private var mutationChance = 0.0f
    private var survivorNumber = 0
    private var grandfathersSize = 0L
    private var childrenSize = 0
    private var initNNRateSize = 0
    private var init2optRateSize = 0
    private var initRandomRateSize = 0

    private var accumTimeStep = 0L

    fun init(key: ResultKey): Resolver {
        this.stepsNo = key.steps
        this.populationSize = key.population
        this.mutationChance = key.mutation
        this.survivorNumber = (populationSize * key.survivor).toInt()
        this.grandfathersSize = (populationSize * key.grandfather).toLong()
        this.initNNRateSize = (populationSize * key.initNnRate).toInt()
        this.init2optRateSize = (populationSize * key.initTwoOptRate).toInt()
        this.initRandomRateSize = populationSize - initNNRateSize - init2optRateSize
        this.childrenSize = (populationSize - grandfathersSize).toInt()
        val points = pointRepository.getPoints()
        crossoverService.init(points.size, childrenSize)
        costService.init(points)

        return this
    }

    fun process(): PathResult {
        var children = createChildren()
        var parents: List<PathResult> = emptyList()
        for (i in 0 until stepsNo) {
            val stepTime = measureTimeMillis {
                parents = sortAndKill(children)

                children = crossOverAndMutateAndScore(parents)
            }

            logProgress(i + 1, parents, stepTime)
        }

        return parents.first()
    }

    private fun createChildren(): List<PathResult> {
        var population: List<PathResult> = emptyList()
        measureTimeMillis {
            population =
                initialPopulationCreator.create(pointRepository.getIds(), init2optRateSize, initNNRateSize, initRandomRateSize)
                    .map { PathResult(it, score(it)) }
        }.let {
            println("Initial population created in ${df2.format(it / 1000.0)}s," +
                    " ${(it / population.size.toFloat())*100}ms per 100 paths")
        }
        return population
    }

    private fun logProgress(i: Int, parents: List<PathResult>, stepTime: Long) {
        accumTimeStep += stepTime
        if (i % 100 == 0) {
            val f = parents.first().result
            val l = parents.last().result.toFloat()
            val percent = if (i == stepsNo) "DONE!" else dfP.format(i / (stepsNo / 100.0)) + "%"

            println(
                "Progress: $percent, " +
                        "best:$green $f$reset, " +
                        "range: ${df.format(l / f * 100.0F)}% " +
                        "time: ${df.format(accumTimeStep / 1000.0)}s"
            )
            accumTimeStep = 0L
        }
    }

    private fun crossOverAndMutateAndScore(parents: List<PathResult>): List<PathResult> = Stream.concat(
        parents.stream().limit(grandfathersSize),
        crossoverService.crossover(parents)
            .peek { mutate(it) }
            .map { PathResult(it, score(it)) }
    ).toList()

    private fun mutate(path: Path) {
        if ((Random.nextFloat() < mutationChance)) {
            val i = Random.nextInt(path.size)
            val j = Random.nextInt(path.size)

            val t = path[i]
            path[i] = path[j]
            path[j] = t
        }
    }

    private fun sortAndKill(
        children: List<PathResult>,
    ): List<PathResult> {
        val sorted = children
            .parallelStream()
            .sorted { a, b -> a.result.compareTo(b.result) }
            .toList()
        arrayProvider.toReuse(sorted.stream().skip(grandfathersSize).map { it.path })
        return sorted.take(survivorNumber)
    }

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