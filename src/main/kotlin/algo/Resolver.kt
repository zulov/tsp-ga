package pl.zulov.algo

import pl.zulov.data.PathResult
import pl.zulov.data.PointRepository
import kotlin.random.Random
import kotlin.system.measureNanoTime

const val STEPS_NO = 5000
const val POPULATION_SIZE = 1000
const val SURVIVOR_RATE = 0.9
const val MUTATION_CHANCE = 0.05
const val SURVIVOR_NUMBER = (POPULATION_SIZE * SURVIVOR_RATE).toInt()
const val CHILDREN_TO_PARENTS_SIZE = (POPULATION_SIZE * 0.9).toInt()

class Resolver(
    val pointRepository: PointRepository
) {
    private val crossoverService = CrossoverService()
    private val costService = CostService()
    private var rateSortKillTime = 0L
    private var crossoverTime = 0L

    fun process(): PathResult {
        val points: List<Int> = pointRepository.getPoints().map { it.id }
        costService.init(pointRepository.getPoints())
        crossoverService.init(points.size)
        var children: MutableList<List<Int>> = createInitialPopulation(points).toMutableList()
        var parents: List<PathResult> = emptyList()
        for (i in 0 until STEPS_NO) {
            parents = rateSortKill(children)

            children = crossOverAndMutate(parents)

            children += parents.take(POPULATION_SIZE - children.size).map { it.path }
            if ((i + 1) % 100 == 0) {
                println("Progress: ${(i + 1)}/$STEPS_NO, best: ${parents.first().result}, worst: ${parents.last().result}")
            }
        }

        println("Crossover time: ${crossoverTime / 1000000} ms")
        println("RateSortKill time: ${rateSortKillTime / 1000000} ms")
        return parents.first()
    }

    private fun crossOverAndMutate(parents: List<PathResult>): MutableList<List<Int>> {
        val result: MutableList<List<Int>>
        measureNanoTime {
            result = crossoverService.crossover(parents.map { it.path }, CHILDREN_TO_PARENTS_SIZE)
                .map { mutate(it) }
                .toMutableList()
        }.let { crossoverTime += it }
        return result
    }

    private fun mutate(path: List<Int>): List<Int> =
        if ((Random.nextFloat() < MUTATION_CHANCE)) {
            val oldPoints = path
            val j = Random.nextInt(oldPoints.size)
            val i = Random.nextInt(oldPoints.size)

            val newPath = oldPoints.toMutableList()
            newPath[i] = oldPoints[j]
            newPath[j] = oldPoints[i]
            newPath
        } else {
            path
        }

    private fun createInitialPopulation(points: List<Int>): List<List<Int>> =
        List(POPULATION_SIZE) { points.shuffled() }

    private fun rateSortKill(
        children: List<List<Int>>,
    ): List<PathResult> {
        var result: List<PathResult>
        measureNanoTime {
            result = children.map { PathResult(score(it), it) }
                .sortedBy { it.result }
                .take(SURVIVOR_NUMBER)
        }.let { rateSortKillTime += it }

        return result
    }

    private fun score(path: List<Int>): Int {
        var totalCost = 0
        for (i in path.indices) {
            totalCost += costService.getCost(path[i], path[(i + 1) % path.size])
        }
        return totalCost
        //return (path + path[0]).zipWithNext { f, t -> costService.getCost(f, t) }.sum()
    }

}