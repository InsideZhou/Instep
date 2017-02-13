package instep.collection

import org.testng.annotations.Test

object AssocArrayTest {
    @Test
    fun assocArrayStringKeyCase() {
        val caseInsensitiveArray = AssocArray(true)

        caseInsensitiveArray["name"] = "name"
        assert(caseInsensitiveArray["name"] == "name")
        assert(caseInsensitiveArray["NAME"] == "name")

        caseInsensitiveArray["name2"] = "name2"
        assert(caseInsensitiveArray["name2"] == "name2")
        assert(caseInsensitiveArray["NAME2"] == "name2")

        caseInsensitiveArray["NAME3"] = "name3"
        assert(caseInsensitiveArray["name3"] == "name3")
        assert(caseInsensitiveArray["NAME3"] == "name3")

        val array = AssocArray()
        array["name"] = "name"
        assert(array["name"] == "name")
        assert(array["NAME"] == null)
    }
}