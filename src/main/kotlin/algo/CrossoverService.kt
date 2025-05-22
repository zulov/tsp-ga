package pl.zulov.algo

import pl.zulov.data.PathResult
import java.util.stream.Stream
import kotlin.random.Random

class CrossoverService(
    private val arrayProvider: ArrayProvider,
) {
    private lateinit var arrayOfByteArrays: Array<ByteArray>
    private lateinit var iRange: List<Int>

    fun init(pathSize: Int, childrenSize: Int) {
        arrayOfByteArrays = Array(childrenSize) { ByteArray(pathSize) }
        iRange = (0 until childrenSize).toList()
    }

    fun crossover(parents: List<PathResult>): Stream<Path> =
        iRange.parallelStream()
            .map { crossover(it, parents.random().path, parents.random().path) }

    fun crossover(
        i: Int,
        parent1: Path,
        parent2: Path,
    ): Path {
        val notFromParent1 = arrayOfByteArrays[i]
        notFromParent1.fill(1)
        val pivot = Random.nextInt(0, parent1.size)
        val result = arrayProvider.getAndFill(i,parent1)

        for (i in pivot until parent1.size) {
            notFromParent1[parent1[i].toInt()] = 0
        }
        var index = 0

        for (id in parent2) {
            result[index] = id

            index += notFromParent1[id.toInt()]
        }
        result[pivot] = parent1[pivot]
        return result
    }

}