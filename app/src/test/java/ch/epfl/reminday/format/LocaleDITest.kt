package ch.epfl.reminday.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class LocaleDITest {

    @Test
    fun providesSystemDefaultLocale() {
        assertEquals(Locale.getDefault(), LocaleDI.provideLocale())
    }
}