package instep.cache

import org.testng.annotations.Test

object InstepCacheTest {
    @Test
    fun normalizeKey() {
        val original = """12390_fdja-AFA:j:;\/JLF(IHO10321^&%呵呵"""
        val normalized = Cache.normalizeKey(original)

        assert("""12390_fdja_AFA_j_JLF_IHO10321_""" == normalized)
    }
}