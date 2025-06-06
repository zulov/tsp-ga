package pl.zulov.algo

import pl.zulov.data.Point
import kotlin.math.roundToInt
import kotlin.math.sqrt

class CostService {

    private var costs: ShortArray = shortArrayOf()
    private var size: Int = 0
    //TODO init only half of the matrix, because costs are symmetric
    fun init(points: List<Point>) {
        size = points.size
        costs = ShortArray(size * size) { index ->
            val i = index / size
            val j = index % size
            if (i != j) getCost(points[i], points[j]) else 0.toShort()
        }
    }

    fun getCost(f: Id, t: Id): Short = costs[f * size + t]

    fun deltaBelowZero(a: Id, b: Id, c: Id, d: Id): Boolean =
        0 < (getCost(a, b) + getCost(c, d) - getCost(a, c) - getCost(b, d))

    fun getCost(f: Point, t: Point): Short =
        sqrt((t.x - f.x) * (t.x - f.x) + (t.y - f.y) * (t.y - f.y)).roundToInt().toShort()

    fun score(path: Path): Int {
        var totalCost = 0
        var from = path.first()
        for (to in path) {
            totalCost += getCost(from, to)
            from = to
        }

        totalCost += getCost(path.last(), path.first())
        return totalCost
    }
}