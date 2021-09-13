package ch.epfl.reminday.util.constant

import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.ui.activity.BirthdayEditActivity

object ArgumentNames {

    /**
     * The argument for holding a single [Birthday]
     */
    const val BIRTHDAY = "single_birthday"

    /**
     * The ordinal of the mode in which the [BirthdayEditActivity] should proceed.
     * Should be set to the ordinal of a value of [BirthdayEditActivity.Mode]
     */
    const val BIRTHDAY_EDIT_MODE_ORDINAL = "edit_mode"
}