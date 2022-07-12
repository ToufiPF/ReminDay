package ch.epfl.reminday.util

import ch.epfl.reminday.data.birthday.AdditionalInformation
import ch.epfl.reminday.data.birthday.Birthday
import io.github.serpro69.kfaker.Faker
import io.github.serpro69.kfaker.faker
import java.time.MonthDay
import java.time.Year
import kotlin.random.Random
import kotlin.random.asJavaRandom

@Suppress("unused", "MemberVisibilityCanBePrivate")
object Mocks {

    val rng = Random(System.currentTimeMillis())

    fun makeFaker(): Faker = faker {
        fakerConfig {
            locale = "en"
            random = rng.asJavaRandom()
            uniqueGeneratorRetryLimit = 500
        }
    }

    fun birthdays(size: Int, yearKnown: (Int) -> Boolean): Array<Birthday> {
        val faker = makeFaker()
        return Array(size) { idx ->
            Birthday(
                faker.name.unique.name(),
                MonthDay.of(rng.nextInt(1, 13), rng.nextInt(1, 29)),
                if (yearKnown(idx)) Year.of(rng.nextInt(1900, Year.now().value + 1))
                else null,
            )
        }
    }

    fun birthday(yearKnown: Boolean) = birthdays(1) { yearKnown }.first()

    fun additionalInfo(personName: String): AdditionalInformation {
        val faker = makeFaker()
        return AdditionalInformation(
            System.currentTimeMillis(),
            personName,
            faker.chuckNorris.fact()
        )
    }
}
