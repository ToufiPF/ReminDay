package ch.epfl.reminday.testutils

import ch.epfl.reminday.testutils.MockitoMatchers.any
import org.mockito.ArgumentCaptor
import org.mockito.Mockito

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
     * Android's Mockito argument captors are made for Java,
     * so [ArgumentCaptor.capture] always returns nullable classes.
     *
     * To make it work with kotlin's functions, need to explicitly cast
     * the return value to it's non-null version.
     */
    fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()
}