package de.crazydev22.kts.wrappers

import java.util.Objects

abstract class JvmWrapper<T>(
    private val direct: T
) {
    fun direct() = direct

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JvmWrapper<*>) return false
        return Objects.equals(direct, other.direct)
    }
    override fun hashCode() = Objects.hashCode(direct)
    override fun toString() = direct.toString()
}
