package pl.zulov.algo

import pl.zulov.data.Point
import kotlin.math.roundToInt
import kotlin.math.sqrt

class CostService {

    private var costs:Array<Array<Int>> = emptyArray()

    fun init(points: List<Point>){
        costs = Array(points.size) { i ->
            Array(points.size) { j ->
                if (i != j) getCost(points[i], points[j]) else 0
            }
        }
    }

    fun getCost(f: Int, t: Int):Int = costs[f][t]

    fun getCost(f: Point, t: Point): Int =
        sqrt((t.x - f.x) * (t.x - f.x) + (t.y - f.y) * (t.y - f.y)).roundToInt()

}