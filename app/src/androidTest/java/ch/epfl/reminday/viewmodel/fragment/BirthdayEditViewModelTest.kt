package ch.epfl.reminday.viewmodel.fragment

import ch.epfl.reminday.format.calendar.MyGregorianCalendar
import ch.epfl.reminday.format.calendar.MyGregorianCalendar.Field
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class BirthdayEditViewModelTest {

    private val calendar = MyGregorianCalendar()
    private val minYear = calendar.minimumSupportedDate().year

    lateinit var viewModel: BirthdayEditViewModel

    @Before
    fun init(): Unit = runBlocking(Dispatchers.Main) {
        viewModel = BirthdayEditViewModel()
    }

    @Test
    fun isDayBeforeMonthIsCorrectForKnownExamples(): Unit = runBlocking(Dispatchers.Main) {
        assertTrue(viewModel.isDayBeforeMonth(Locale.FRANCE))
        assertTrue(viewModel.isDayBeforeMonth(Locale.ITALIAN))
        assertFalse(viewModel.isDayBeforeMonth(Locale.ENGLISH))
    }

    @Test
    fun contentsAreUpdatedAfterSetField(): Unit = runBlocking(Dispatchers.Main) {
        viewModel.setField(Field.YEAR, "1970")
        viewModel.setField(Field.MONTH, "10")
        viewModel.setField(Field.DAY_OF_MONTH, "08")

        assertEquals(1970, viewModel.getField(Field.YEAR))
        assertEquals(10, viewModel.getField(Field.MONTH))
        assertEquals(8, viewModel.getField(Field.DAY_OF_MONTH))

        assertEquals("1970", viewModel.yearEditContent.value)
        assertEquals("10", viewModel.monthEditContent.value)
        assertEquals("8", viewModel.dayEditContent.value)
    }

    @Test
    fun contentIsConstrainedToValidDates(): Unit = runBlocking(Dispatchers.Main) {
        viewModel.setField(Field.YEAR, "1000")
        viewModel.setField(Field.MONTH, "0")
        viewModel.setField(Field.DAY_OF_MONTH, "0")

        assertEquals(minYear, viewModel.getField(Field.YEAR))
        assertEquals(1, viewModel.getField(Field.MONTH))
        assertEquals(1, viewModel.getField(Field.DAY_OF_MONTH))

        viewModel.setField(Field.DAY_OF_MONTH, "10")

        assertEquals(minYear, viewModel.getField(Field.YEAR))
        assertEquals(1, viewModel.getField(Field.MONTH))
        assertEquals(10, viewModel.getField(Field.DAY_OF_MONTH))
    }

    @Test
    fun yearCanBeWrittenFrom1CharTo4(): Unit = runBlocking(Dispatchers.Main) {
        viewModel.setField(Field.YEAR, null)
        assertEquals(null, viewModel.getField(Field.YEAR))
        assertEquals("", viewModel.yearEditContent.value)

        // viewModel shouldn't update the content while user is typing
        viewModel.setField(Field.YEAR, "1")
        assertEquals(minYear, viewModel.getField(Field.YEAR))
        assertEquals("", viewModel.yearEditContent.value)

        viewModel.setField(Field.YEAR, "19")
        assertEquals(minYear, viewModel.getField(Field.YEAR))
        assertEquals("", viewModel.yearEditContent.value)

        viewModel.setField(Field.YEAR, "198")
        assertEquals(minYear, viewModel.getField(Field.YEAR))
        assertEquals("", viewModel.yearEditContent.value)

        // now we should update the content
        viewModel.setField(Field.YEAR, "1987")
        assertEquals(1987, viewModel.getField(Field.YEAR))
        assertEquals("1987", viewModel.yearEditContent.value)
    }
}