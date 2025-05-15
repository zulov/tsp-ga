package pl.zulov.algo

import pl.zulov.data.PathResult
import java.util.stream.Stream
import kotlin.random.Random

class CrossoverService {
    private lateinit var arrayOfByteArrays: Array<ByteArray>

    fun init(populationSize: Int, pathSize: Int) {
        arrayOfByteArrays = Array(populationSize) { ByteArray(pathSize) }
    }

    fun crossover(parents: List<PathResult>, populationSize: Long): Stream<Path> =
        (0 until populationSize).toList().parallelStream()
            .map { crossover(it, parents.random().path, parents.random().path) }

    fun crossover(
        i: Long,
        parent1: Path,
        parent2: Path,
    ): Path {
        val usedInParent1 = arrayOfByteArrays[i.toInt()]
        usedInParent1.fill(1)
        val pivot = Random.nextInt(0, parent1.size)
        val result = parent1.copyOf()

        for (i in pivot until parent1.size) {
            usedInParent1[parent1[i].toInt()] = 0
        }
        var index = 0

        for (id in parent2) {
            result[index] = id

            index += usedInParent1[id.toInt()]
        }
        result[pivot] = parent1[pivot]
        if (result.distinct().size != result.size) {
            throw IllegalStateException("Duplicate values in result: $result,pivot:$pivot")
        }
        return result
    }

}