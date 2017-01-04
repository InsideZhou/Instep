package instep

import instep.collection.AssocArray
import org.testng.annotations.Test

object InstepCollectionTest {
    @Test
    fun assocArrayStringKeyCase() {
        val array = AssocArray(true)

        array["name"] = "name"
        assert(array["name"] == "name")
        assert(array["NAME"] == "name")

        array["name2"] = "name2"
        assert(array["name2"] == "name2")
        assert(array["NAME2"] == "name2")

        array["NAME3"] = "name3"
        assert(array["name3"] == "name3")
        assert(array["NAME3"] == "name3")

        val caseSensitiveArray = AssocArray(false)
        caseSensitiveArray["name"] = "name"
        assert(caseSensitiveArray["name"] == "name")
        assert(caseSensitiveArray["NAME"] == null)
    }
}