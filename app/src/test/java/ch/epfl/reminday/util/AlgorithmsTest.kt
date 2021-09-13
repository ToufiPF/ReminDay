package ch.epfl.reminday.util

import ch.epfl.reminday.util.Algorithms.longestCommonSubsequence
import org.junit.Assert.assertEquals
import org.junit.Test

class AlgorithmsTest {

    @Test
    fun lcsWorksOnEdgeCases() {
        assertEquals("", longestCommonSubsequence("", ""))
        assertEquals("", longestCommonSubsequence("", "0000"))
        assertEquals("", longestCommonSubsequence("1111", ""))
        assertEquals("", longestCommonSubsequence("12345", "a"))
    }

    @Test
    fun lcsWorksOnKnownExamples() {
        assertEquals("a", longestCommonSubsequence("abba", "acdc"))
        assertEquals("456789", longestCommonSubsequence("123456789", "45abc4567894"))
    }
}