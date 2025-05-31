package pl.zulov

import pl.zulov.algo.Resolver
import pl.zulov.algo.df
import pl.zulov.algo.df2
import pl.zulov.algo.timeFormatter
import pl.zulov.data.PathResult
import pl.zulov.data.PointRepository
import java.time.LocalTime
import kotlin.collections.listOf
import kotlin.system.measureTimeMillis

val STEPS_NO = listOf(2_000)
val POPULATION_SIZE = listOf(2_000)
val SURVIVOR_RATE = listOf(0.8F)
val MUTATION_CHANCE = listOf(0.3F)
val TWO_OPT_MUTATION_CHANCE = listOf(0.1f)
val TWO_OPT_MUTATION_LIMIT = listOf(3)
val GRANDFATHER_RATE = listOf(0.1f)
val NN_RATE = listOf(0.1f)
val TWO_OPT_RATE = listOf(0.1f)

val params = STEPS_NO.flatMap { steps ->
    POPULATION_SIZE.flatMap { population ->
        SURVIVOR_RATE.flatMap { survivor ->
            MUTATION_CHANCE.flatMap { mutation ->
                TWO_OPT_MUTATION_CHANCE.flatMap { twpOptMutation ->
                    TWO_OPT_MUTATION_LIMIT.flatMap { twpOptMutationLimit ->
                        GRANDFATHER_RATE.flatMap { grandfather ->
                            TWO_OPT_RATE.flatMap { twoOptRate ->
                                NN_RATE.map { initNnRate ->
                                    ResultKey(
                                        steps = steps,
                                        population = population,
                                        survivor = survivor,
                                        mutation = mutation,
                                        twoOptMutation = twpOptMutation,
                                        twoOptMutationLimit = twpOptMutationLimit,
                                        grandfather = grandfather,
                                        initTwoOptRate = twoOptRate,
                                        initNnRate = initNnRate
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun main() {
    printTime("Start: ")
    val pointRepository = PointRepository()
    pointRepository.load("gr9882")

    val results: MutableList<Pair<ResultKey, Result>> = mutableListOf()
    val resolver = Resolver(pointRepository)
    params.forEachIndexed { i, p ->
        var result: PathResult
        val processTime = measureTimeMillis {
            result = resolver.init(p).process()
        }
        println("$p = ${result.result}")
        results.add(p to Result(result.result, (processTime / 1000).toInt()))
        println("${(i + 1)}/${params.size} Time process: ${processTime / 1000}s, per step: ${processTime / p.steps} ms")
    }

    println("Results:")
    results.sortedBy { it.second.distance }
        .forEach { println("${it.first} -> ${it.second.distance}, ${it.second.time}s") }
    println("Grouped:")
    ResultKey.params().forEach { (name, fn) -> groupAndPrintResults(results, name, fn) }

    printTime("End: ")
}

private fun printTime(prefix: String): Unit =
    println(prefix + LocalTime.now().format(timeFormatter))

fun groupAndPrintResults(results: List<Pair<ResultKey, Result>>, name: String, getter: (ResultKey) -> String) =
    results.map { getter(it.first) to it.second }
        .groupBy { it.first }
        .map { it.key to Result(avgDist(it.value) , avgTime(it.value)) }
        .sortedBy { it.second.distance }
        .map { (value, result) -> "\t$value -> ${df.format(result.distance)}, ${result.time}s" }
        .takeIf { it.size > 1 }
        ?.let {
            println("By $name:")
            it.forEach { line -> println(line) }
        }

private fun avgTime(value: List<Pair<String, Result>>): Int =
    value.map { it.second.time }.average().toInt()

private fun avgDist(value: List<Pair<String, Result>>): Int =
    value.map { it.second.distance }.average().toInt()

data class Result(
    val distance: Int,
    val time: Int,
)

data class ResultKey(
    val steps: Int,
    val population: Int,
    val survivor: Float,
    val mutation: Float,
    val twoOptMutation: Float,
    val twoOptMutationLimit: Int,
    val grandfather: Float,
    val initTwoOptRate: Float,
    val initNnRate: Float,
) {
    override fun toString(): String =
        "Steps: $steps; Pop: $population; " +
                "Survivor: ${df.format(survivor)}; " +
                "Mutation: ${df.format(mutation)}; " +
                "2 Opt Mutation: ${df.format(twoOptMutation)}; " +
                "2 Opt Mutation Limit: $twoOptMutationLimit; " +
                "Grandfather: ${df.format(grandfather)}; " +
                "2 Opt: ${df.format(initTwoOptRate)}; " +
                "NN rate: ${df2.format(initNnRate)}"

    companion object {
        fun params(): List<Pair<String, (ResultKey) -> String>> =
            listOf(
                "Survivor" to { x -> x.survivor.toString() },
                "Mutation" to { x -> x.mutation.toString() },
                "2 Opt mutation" to { x -> x.twoOptMutation.toString() },
                "2 Opt mutation Limit" to { x -> x.twoOptMutationLimit.toString() },
                "Population" to { x -> x.population.toString() },
                "Grandfather" to { x -> x.grandfather.toString() },
                "InitTwoOptRate" to { x -> x.initTwoOptRate.toString() },
                "InitNnRate" to { x -> x.initNnRate.toString() },
            )
    }
}