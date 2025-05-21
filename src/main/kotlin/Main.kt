package pl.zulov

import pl.zulov.algo.Resolver
import pl.zulov.algo.df
import pl.zulov.algo.df2
import pl.zulov.algo.timeFormatter
import pl.zulov.data.PointRepository
import java.time.LocalTime
import kotlin.collections.listOf
import kotlin.system.measureTimeMillis

val pointRepository = PointRepository()

val STEPS_NO = listOf(1000)
val POPULATION_SIZE = listOf(10_000)
val SURVIVOR_RATE = listOf(0.8F)
val MUTATION_CHANCE = listOf(0.3F)
val GRANDFATHER_RATE = listOf(0.9f)
val NN_RATE = listOf(0.1f)
fun main() {
    println(LocalTime.now().format(timeFormatter))
    pointRepository.load("xit1083")

    val results: MutableMap<ResultKey, Int> = mutableMapOf()
    val resolver = Resolver(pointRepository)
    prepareParameters()
        .forEach { p ->
            val processTime = measureTimeMillis {
                val (_, distance) = resolver.init(p).process()
                results[p] = distance
                println("$p = $distance")
            }
            println("Time process: ${processTime / 1000}s, per step: ${processTime / p.steps} ms")
        }

    println("Results:")
    results.entries.sortedBy { it.value }.forEach {
        println("${it.key} -> ${it.value}")
    }
    println("Grouped:")
    groupAndPrintResults(results, "survivor") { x -> x.survivor.toString() }
    groupAndPrintResults(results, "mutation") { x -> x.mutation.toString() }
    groupAndPrintResults(results, "population") { x -> x.population.toString() }
    groupAndPrintResults(results, "grandfather") { x -> x.grandfather.toString() }
    groupAndPrintResults(results, "initNnRate") { x -> x.initNnRate.toString() }
    println(LocalTime.now().format(timeFormatter))
}

private fun prepareParameters(): List<ResultKey> = STEPS_NO.flatMap { steps ->
    POPULATION_SIZE.flatMap { population ->
        SURVIVOR_RATE.flatMap { survivor ->
            MUTATION_CHANCE.flatMap { mutation ->
                GRANDFATHER_RATE.flatMap { grandfather ->
                    NN_RATE.map { initNnRate ->
                        ResultKey(steps, population, survivor, mutation, grandfather, initNnRate)
                    }
                }
            }
        }
    }
}

fun groupAndPrintResults(results: Map<ResultKey, Int>, name: String, getter: (ResultKey) -> String) {
    val lines = results.entries.map { getter(it.key) to it.value }
        .groupBy { it.first }
        .map { it.key to it.value.map { it.second }.average() }
        .sortedBy { it.second }
        .map { (value, result) -> "\t$value -> ${df.format(result)}" }
    if (lines.size > 1) {
        println("By $name:")
        lines.forEach { println(it) }
    }
}


data class ResultKey(
    val steps: Int,
    val population: Int,
    val survivor: Float,
    val mutation: Float,
    val grandfather: Float,
    val initNnRate: Float,
) {
    override fun toString(): String =
        "Steps: $steps; Pop: $population; " +
                "Survivor: ${df.format(survivor)}; " +
                "Mutation: ${df.format(mutation)}; " +
                "Grandfather: ${df.format(grandfather)}; " +
                "NN rate: ${df2.format(initNnRate)}"
}