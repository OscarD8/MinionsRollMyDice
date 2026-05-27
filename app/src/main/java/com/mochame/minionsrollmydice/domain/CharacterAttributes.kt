package com.mochame.minionsrollmydice.domain

/**
 * Pure Kotlin domain model representing the D&D Character Attributes.
 * Enforces business rules and constraints:
 * - Each attribute must be within [0, 5].
 * - The sum of all allocated attribute points must be <= 5.
 */
data class CharacterAttributes(
    val wisdom: Int = 0,
    val insight: Int = 0,
    val inspired: Int = 0
) {
    init {
        require(wisdom in 0..5) { "Wisdom must be between 0 and 5" }
        require(insight in 0..5) { "Insight must be between 0 and 5" }
        require(inspired in 0..5) { "Inspired must be between 0 and 5" }
        require(totalAllocated() <= MAX_POINTS) { "Total allocated points cannot exceed $MAX_POINTS" }
    }

    fun totalAllocated(): Int = wisdom + insight + inspired

    fun remainingPoints(): Int = MAX_POINTS - totalAllocated()

    fun canIncrement(attribute: AttributeType): Boolean {
        if (totalAllocated() >= MAX_POINTS) return false
        return when (attribute) {
            AttributeType.WISDOM -> wisdom < 5
            AttributeType.INSIGHT -> insight < 5
            AttributeType.INSPIRED -> inspired < 5
        }
    }

    fun canDecrement(attribute: AttributeType): Boolean {
        return when (attribute) {
            AttributeType.WISDOM -> wisdom > 0
            AttributeType.INSIGHT -> insight > 0
            AttributeType.INSPIRED -> inspired > 0
        }
    }

    fun increment(attribute: AttributeType): CharacterAttributes {
        if (!canIncrement(attribute)) return this
        return when (attribute) {
            AttributeType.WISDOM -> copy(wisdom = wisdom + 1)
            AttributeType.INSIGHT -> copy(insight = insight + 1)
            AttributeType.INSPIRED -> copy(inspired = inspired + 1)
        }
    }

    fun decrement(attribute: AttributeType): CharacterAttributes {
        if (!canDecrement(attribute)) return this
        return when (attribute) {
            AttributeType.WISDOM -> copy(wisdom = wisdom - 1)
            AttributeType.INSIGHT -> copy(insight = insight - 1)
            AttributeType.INSPIRED -> copy(inspired = inspired - 1)
        }
    }

    fun calculateTotalModifier(): Int {
        // Direct D&D impact: modifier is the sum of all attribute points.
        return wisdom + insight + inspired
    }

    companion object {
        const val MAX_POINTS = 5
    }
}

enum class AttributeType {
    WISDOM,
    INSIGHT,
    INSPIRED
}
