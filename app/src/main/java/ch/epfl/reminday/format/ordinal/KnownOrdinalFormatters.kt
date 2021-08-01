package ch.epfl.reminday.format.ordinal

internal object KnownOrdinalFormatters {

    val french = OrdinalFormatter { number ->
        when {
            number == 1 -> "er"
            0 < number -> "e"
            else -> ""
        }
    }

    val english = OrdinalFormatter { number ->
        when {
            number in 11..20 -> "th"
            number % 10 == 1 -> "st"
            number % 10 == 2 -> "nd"
            number % 10 == 3 -> "rd"
            else -> "th"
        }
    }
}