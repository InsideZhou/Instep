package instep.dao

import instep.dao.impl.DefaultPlanFromText

interface PlanFromText : Plan<PlanFromText>, Expression<PlanFromText> {
    override fun clone(): PlanFromText

    companion object : PlanFromTextFactory {
        override fun createInstance(txt: String): PlanFromText {
            return DefaultPlanFromText(txt)
        }
    }
}

interface PlanFromTextFactory {
    fun createInstance(txt: String): PlanFromText
}