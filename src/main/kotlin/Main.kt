package pl.zulov

import pl.zulov.algo.Resolver
import pl.zulov.data.PointRepository
import java.text.DecimalFormat
import kotlin.collections.listOf
import kotlin.system.measureTimeMillis

val pointRepository = PointRepository()

val STEPS_NO = listOf(3000)
val POPULATION_SIZE = listOf(30_000)
val SURVIVOR_RATE = listOf(0.4F, 0.5F, 0.6F, 0.7F, 0.8F, 0.9F)
val MUTATION_CHANCE = listOf(0.02F, 0.05F, 0.1F, 0.2F, 0.3F)
val GRANDFATHER_RATE = listOf(0.1F, 0.2F, 0.3F)
private val df = DecimalFormat("0.0")
fun main() {
    measureTimeMillis {
        pointRepository.load("xit1083")
    }.let { println("Time to load file: $it ms") }

    val results: MutableMap<ResultKey, Int> = mutableMapOf()

    STEPS_NO.forEach { steps ->
        POPULATION_SIZE.forEach { population ->
            SURVIVOR_RATE.forEach { survivor ->
                MUTATION_CHANCE.forEach { mutation ->
                    GRANDFATHER_RATE.forEach { grandfather ->
                        val processTime = measureTimeMillis {
                            val resolver = Resolver(pointRepository, steps, population, survivor, mutation, grandfather)
                            val (distance, order) = resolver.process()
                            val key = ResultKey(steps, population, survivor, mutation, grandfather)
                            results[key] = distance
                            println("Distance: $distance")
                            println(key)
                        }
                        println("Time process: $processTime ms, and per step: ${processTime / steps} ms")
                    }
                }
            }
        }
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

}

fun groupAndPrintResults(results: Map<ResultKey, Int>, name: String, getter: (ResultKey) -> String) {
    println("By $name:")
    results.entries.map { getter(it.key) to it.value }
        .groupBy { it.first }
        .map { it.key to it.value.map { it.second }.average() }
        .sortedBy { it.second }
        .forEach { (value, result) ->
            println("\t$value -> ${df.format(result)}")
        }
}


data class ResultKey(
    val steps: Int,
    val population: Int,
    val survivor: Float,
    val mutation: Float,
    val grandfather: Float
) {
    override fun toString(): String =
        "Steps: $steps; Pop: $population; " +
                "Survivor: ${df.format(survivor)}; " +
                "Mutation: ${df.format(mutation)}; " +
                "Grandfather: ${df.format(grandfather)}"
}