package pl.zulov.data

import pl.zulov.algo.CostService
import java.io.File
import kotlin.system.measureTimeMillis

class PointRepository {

    private val points = mutableListOf<Point>()
    private val costService = CostService()

    fun load(fileName: String): List<Point> {
        val timeTaken = measureTimeMillis {
            val file = File(javaClass.classLoader.getResource("$fileName.tsp")?.file!!)
            if (!file.exists()) {
                throw IllegalArgumentException("File not found: $fileName.tsp")
            }
            file.readLines().drop(7)
                .forEach { line ->
                    val lineData = line.split(" ").map { it.trim() }
                    if (lineData.size < 3) return@forEach
                    points.add(Point(lineData[0].toInt()-1, lineData[1].toFloat(), lineData[2].toFloat()))
                }
            val resultFile = File(javaClass.classLoader.getResource("$fileName.result")?.file!!)
            if (resultFile.exists()) {
                val data = resultFile.readLines()
                    .map { points[it.toInt() - 1] }.toMutableList()
                data += data[0]
                val sum = data
                    .zipWithNext { f, t -> costService.getCost(f, t)}.sum()
                println("Sum of distances: $sum")
            }

        }
        println("Time taken to load file: $timeTaken ms")
        return points
    }

    fun getPoints(): List<Point> = points

}