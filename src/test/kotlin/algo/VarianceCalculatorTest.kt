package pl.zulov.algo

import pl.zulov.data.PathResult
import kotlin.math.abs
import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VarianceCalculatorTest {

    private val calculator = VarianceCalculator()

    private fun pathResult(vararg cities: Short) = PathResult(shortArrayOf(*cities), 0)

    @Test
    fun `empty list returns 0`() {
        val result = calculator.calculateVariance(emptyList())
        assertEquals(0.0f, result)
    }

    @Test
    fun `single parent returns 0`() {
        val result = calculator.calculateVariance(listOf(pathResult(0, 1, 2)))
        assertEquals(0.0f, result)
    }

    @Test
    fun `identical parents return 0 - fully converged`() {
        val parents = listOf(
            pathResult(0, 1, 2),
            pathResult(0, 1, 2),
            pathResult(0, 1, 2),
        )
        val result = calculator.calculateVariance(parents)
        assertEquals(0.0f, result)
    }

    @Test
    fun `two completely different parents return 1 - max diversity`() {
        // At each position, two distinct cities, each appearing once
        // entropy = ln(2)/ln(2) = 1.0 at every position
        val parents = listOf(
            pathResult(0, 1),
            pathResult(1, 0),
        )
        val result = calculator.calculateVariance(parents)
        assertEquals(1.0f, result, 0.001f)
    }

    @Test
    fun `three parents forming Latin square return 1 - max diversity`() {
        // Each city appears exactly once at each position across all 3 parents
        // entropy = ln(3)/ln(3) = 1.0 at every position
        val parents = listOf(
            pathResult(0, 1, 2),
            pathResult(1, 2, 0),
            pathResult(2, 0, 1),
        )
        val result = calculator.calculateVariance(parents)
        assertEquals(1.0f, result, 0.001f)
    }

    @Test
    fun `partial convergence returns value between 0 and 1`() {
        // Position 0: all have city 0 → entropy = 0
        // Position 1: two have city 1, one has city 2 → entropy > 0 but < max
        // Position 2: two have city 2, one has city 1 → entropy > 0 but < max
        val parents = listOf(
            pathResult(0, 1, 2),
            pathResult(0, 1, 2),
            pathResult(0, 2, 1),
        )
        val result = calculator.calculateVariance(parents)
        assertTrue(result > 0.0f, "Expected diversity > 0 but was $result")
        assertTrue(result < 1.0f, "Expected diversity < 1 but was $result")
    }

    @Test
    fun `partial convergence has correct value`() {
        // 3 parents, 3 positions:
        // Position 0: [0,0,0] → all same → entropy = 0
        // Position 1: [1,1,2] → 2/3 city1, 1/3 city2 → H = -(2/3*ln(2/3) + 1/3*ln(1/3))
        // Position 2: [2,2,1] → same distribution as position 1
        val parents = listOf(
            pathResult(0, 1, 2),
            pathResult(0, 1, 2),
            pathResult(0, 2, 1),
        )

        val p1 = 2.0 / 3.0
        val p2 = 1.0 / 3.0
        val positionEntropy = -(p1 * ln(p1) + p2 * ln(p2))
        val normalizedPositionEntropy = positionEntropy / ln(3.0)
        // Position 0 contributes 0, positions 1 and 2 contribute normalizedPositionEntropy each
        val expected = (0.0 + normalizedPositionEntropy + normalizedPositionEntropy) / 3.0

        val result = calculator.calculateVariance(parents)
        assertTrue(abs(result - expected.toFloat()) < 0.001f,
            "Expected ~${expected.toFloat()} but was $result")
    }

    @Test
    fun `larger population with mixed diversity`() {
        // 4 parents, 4 positions
        val parents = listOf(
            pathResult(0, 1, 2, 3),
            pathResult(0, 1, 2, 3),
            pathResult(0, 1, 3, 2),
            pathResult(0, 1, 3, 2),
        )
        val result = calculator.calculateVariance(parents)

        // Positions 0,1: all agree → entropy = 0
        // Positions 2,3: 2 have one city, 2 have another → entropy = ln(2)/ln(4) = 0.5
        val expected = (0.0 + 0.0 + 0.5 + 0.5) / 4.0
        assertTrue(abs(result - expected.toFloat()) < 0.001f,
            "Expected ~${expected.toFloat()} but was $result")
    }

    @Test
    fun `diversity decreases as population converges`() {
        // Simulate convergence: start diverse, then make more parents identical
        val diverse = listOf(
            pathResult(0, 1, 2),
            pathResult(1, 2, 0),
            pathResult(2, 0, 1),
        )

        val partiallyConverged = listOf(
            pathResult(0, 1, 2),
            pathResult(0, 1, 2),
            pathResult(2, 0, 1),
        )

        val fullyConverged = listOf(
            pathResult(0, 1, 2),
            pathResult(0, 1, 2),
            pathResult(0, 1, 2),
        )

        val diverseResult = calculator.calculateVariance(diverse)
        val partialResult = calculator.calculateVariance(partiallyConverged)
        val convergedResult = calculator.calculateVariance(fullyConverged)

        assertTrue(diverseResult > partialResult,
            "Diverse ($diverseResult) should be > partially converged ($partialResult)")
        assertTrue(partialResult > convergedResult,
            "Partially converged ($partialResult) should be > fully converged ($convergedResult)")
        assertEquals(0.0f, convergedResult)
    }
}
