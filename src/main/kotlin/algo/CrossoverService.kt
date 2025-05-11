package pl.zulov.algo

import java.util.stream.Stream
import kotlin.random.Random

class CrossoverService {

    fun crossover(parents: List<Path>, populationSize: Long): Stream<Path> =
        (0 until populationSize).toList().parallelStream().map { crossover(parents.random(), parents.random()) }

    fun crossover(
        parent1: Path,
        parent2: Path,
    ): Path {
        val set = BooleanArray(parent1.size) { false }
        val pivot = Random.nextInt(0, parent1.size)
        val result = parent1.copyOf()

        for (i in 0 until pivot) {
            set[parent1[i].toInt()] = true
        }
        var index = pivot
        //zlikwidowac if
        for (id in parent2) {
            if (!set[id.toInt()]) {
                result[index++] = id
            }
        }
        return result
    }

}