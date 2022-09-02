package ch.epfl.reminday

import java.lang.reflect.Field

object Reflection {

    /**
     * Changes the value of a (java) static final field using reflection.
     * The state won't be reset until the program gets restarted
     * so be careful to rollback any changes you've made.
     * @param field [Field] the field to modify
     * @param value [Any]? the value to set
     */
    fun setFinalStaticField(field: Field, value: Any?) {
        field.isAccessible = true

        field[null] = value

        field.isAccessible = false
    }
}