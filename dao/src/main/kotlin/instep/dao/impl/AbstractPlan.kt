package instep.dao.impl

import instep.dao.Plan

abstract class AbstractPlan<T : Plan<T>> : Plan<T> {
    @Suppress("UNCHECKED_CAST")
    override public fun clone(): T {
        return super.clone() as T
    }
}