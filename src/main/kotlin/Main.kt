package pl.zulov

import pl.zulov.algo.Resolver
import pl.zulov.algo.STEPS_NO
import pl.zulov.data.PointRepository
import kotlin.system.measureTimeMillis

val pointRepository = PointRepository()
val resolver = Resolver(pointRepository)

fun main() {
    val loadTime = measureTimeMillis {
        pointRepository.load("gr9882")
    }
    val processTime = measureTimeMillis {
        val (distance, order) = resolver.process()

        println("Distance: $distance")
        println("Order: ${order.joinToString(",") { it.toString() }}")
    }
    println("Time to load file: $loadTime ms")
    println("Time process: $processTime ms, and per step: ${processTime / STEPS_NO} ms")
}