package pl.zulov.algo

import pl.zulov.data.Point
import kotlin.math.roundToInt
import kotlin.math.sqrt

class CostService {

    private var costs: IntArray = intArrayOf()
    private var size: Int = 0

    fun init(points: List<Point>) {
        size = points.size
        val size = points.size
        costs = IntArray(size * size) { index ->
            val i = index / size
            val j = index % size
            if (i != j) getCost(points[i], points[j]) else 0
        }
    }

    fun getCost(f: Int, t: Int): Int {
        return costs[f * size + t]
    }

    fun getCost(f: Point, t: Point): Int =
        sqrt((t.x - f.x) * (t.x - f.x) + (t.y - f.y) * (t.y - f.y)).roundToInt()

}