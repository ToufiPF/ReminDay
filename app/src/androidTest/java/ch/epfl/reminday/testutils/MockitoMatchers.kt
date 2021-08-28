package ch.epfl.reminday.testutils

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.internal.matchers.VarargMatcher

/**
 * Android's Mockito matchers are made for java, causing several problems with matchers.
 * Use this class helpers if you have problems with mockito in **instrumented tests**.
 */
object MockitoMatchers {
    /**
     * Android's Mockito any() was made for java, it returns a nullable type.
     * Need explicit cast to non-nullable type for kotlin.
     *
     * Note: For primitive types, should still use eg. [Mockito.anyInt] which are non nullable
     * since they return primitives.
     */
    inline fun <reified T> any(): T = Mockito.any(T::class.java)

    /**
     * For symmetry with [any], returns a Mockito matcher that supports nullable values.
     */
    inline fun <reified T> anyNullable(): T? = Mockito.nullable(T::class.java)

    /**
     * Matches any vararg of type T.
     * Use with the spread operator <pre>'*'</pre>.
     */
    inline fun <reified T> anyVararg(): Array<out T> {
        return VarArgMatcher.varArgThat(object : BaseMatcher<Array<out T>>() {
            override fun describeTo(description: Description?) {
                description?.appendText("matches anything")
            }

            override fun matches(item: Any?): Boolean = true
        })
    }

    /**
     * Matcher for varargs.
     */
    class VarArgMatcher<T> @PublishedApi internal constructor(
        private val hamcrestMatcher: Matcher<Array<out T>>
    ) : ArgumentMatcher<Array<out T>>, VarargMatcher {

        companion object {
            inline fun <reified T> varArgThat(hamcrestMatcher: Matcher<Array<out T>>): Array<out T> {
                ArgumentMatchers.argThat(VarArgMatcher(hamcrestMatcher))
                return Array(0) { null as T }
            }
        }

        override fun matches(argument: Array<out T>): Boolean = hamcrestMatcher.matches(argument)
    }

    /**
     * Android's Mockito argument captors are made for Java,
     * so [ArgumentCaptor.capture] always returns nullable classes.
     *
     * To make it work with kotlin's functions, need to explicitly cast
     * the return value to it's non-null version.
     */
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
}