package dev.s7a.ktinventory.util

import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty

internal fun <T> resetableLazy(initializer: () -> T) = ResetableLazy(initializer)

internal class ResetableLazy<T>(
    private val initializer: () -> T,
) {
    private val value = AtomicReference(lazy(initializer))

    operator fun getValue(
        ref: Any?,
        property: KProperty<*>,
    ): T = value.get().getValue(ref, property)

    fun reset() {
        value.set(lazy(initializer))
    }
}
