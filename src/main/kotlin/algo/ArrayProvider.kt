package pl.zulov.algo

import java.util.stream.Stream

class ArrayProvider {

    private var current: MutableList<Path> = mutableListOf()
    private var backup: MutableList<Path> = mutableListOf()

    fun getAndFill(i: Int, parent: Path): Path = if (i < current.size) {
        val child = current[i]
        System.arraycopy(parent, 0, child, 0, parent.size)
        child
    } else {
        parent.copyOf()
    }

    fun toReuse(parents: Stream<Path>) {
        swapCurrentAndBackup()
        backup.clear()
        backup.addAll(parents.toList())
    }

    private fun swapCurrentAndBackup() {
        val temp = current
        current = backup
        backup = temp
    }
}