package ch.epfl.reminday.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import ch.epfl.reminday.R
import ch.epfl.reminday.util.constant.PreferenceNames.GENERAL_PREFERENCES
import io.mockk.CapturingSlot
import io.mockk.EqMatcher
import io.mockk.OfTypeMatcher
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.Month

class AlarmBroadcastReceiverTest {
    companion object {
        private const val ACTION = "ch.epfl.reminday.action.DAILY_BIRTHDAY_CHECK"

        private const val timePrefName = "mocked_pref"
    }

    private lateinit var context: Context
    private lateinit var manager: AlarmManager
    private lateinit var preferences: SharedPreferences
    private lateinit var pendingIntent: PendingIntent

    private lateinit var flagsSlot: CapturingSlot<Int>

    private val sysCurrentTime = 1000L
    private val now = LocalDateTime.of(2023, Month.JANUARY, 1, 12, 30)

    @Before
    fun init() {
        context = mockk(relaxed = true)
        manager = mockk(relaxed = true)
        preferences = mockk(relaxed = false)
        pendingIntent = mockk(relaxed = true)

        flagsSlot = CapturingSlot()

        every { context.getString(R.string.prefs_notification_time) } returns timePrefName
        every { context.getSharedPreferences(GENERAL_PREFERENCES, any()) } returns preferences
    }

    private fun runTest(test: () -> Unit) {
        mockkStatic(ContextCompat::class, PendingIntent::class, LocalDateTime::class) {
            every {
                ContextCompat.getSystemService(context, AlarmManager::class.java)
            } returns manager
            every {
                PendingIntent.getBroadcast(context, any(), any(), capture(flagsSlot))
            } returns pendingIntent
            every { LocalDateTime.now() } returns now

            mockkConstructor(Intent::class) {
                every { anyConstructed<Intent>().setAction(ACTION) } returns mockk()

                // FIXME checking ctor parameters doesn't work
//                every {
//                    constructedWith<Intent>(
//                        EqMatcher(context),
//                        EqMatcher(AlarmBroadcastReceiver::class.java)
//                    ).setAction(ACTION)
//                } returns mockk()

                mockkObject(AlarmBroadcastReceiver.Companion) {
                    every { AlarmBroadcastReceiver.getCurrentTimeMillis() } returns sysCurrentTime

                    test()
                }
            }
        }
    }

    @Test
    fun oneTimeAlarmRequestSendsBroadcastDirectly(): Unit = runTest {
        AlarmBroadcastReceiver.enqueueOneTimeAlarmRequest(context)

        verify { context.sendBroadcast(any()) }

        verify {
            anyConstructed<Intent>().action = ACTION
        }
        // FIXME checking ctor parameters doesn't work
//        verify {
//            constructedWith<Intent>(
//                EqMatcher(context),
//                EqMatcher(AlarmBroadcastReceiver::class.java)
//            ).setAction(ACTION)
//        }
    }

    @Test
    fun periodicAlarmRequestRegistersBroadcastOnSameDay(): Unit = runTest {
        val configuredHour = 14
        val configuredMinutes = 15
        every {
            preferences.getInt(timePrefName, any())
        } returns configuredHour * 60 + configuredMinutes
        val expectedMillis = (60 + 45) * 60 * 1000L

        AlarmBroadcastReceiver.enqueuePeriodicAlarmRequest(context)

        verify {
            manager.setInexactRepeating(
                AlarmManager.RTC,
                sysCurrentTime + expectedMillis,
                AlarmManager.INTERVAL_HALF_DAY,
                pendingIntent
            )
        }
        verify { anyConstructed<Intent>().action = ACTION }
        assertThat(flagsSlot.captured and PendingIntent.FLAG_IMMUTABLE, `is`(not(0)))
        assertThat(flagsSlot.captured and PendingIntent.FLAG_ONE_SHOT, `is`(0))
    }

    @Test
    fun periodicAlarmRequestRegistersBroadcastOnNextDay(): Unit = runTest {
        val configuredHour = 8
        val configuredMinutes = 45
        every {
            preferences.getInt(timePrefName, any())
        } returns configuredHour * 60 + configuredMinutes
        val expectedMillis =
            (((24 + configuredHour - now.hour) * 60) + configuredMinutes - now.minute) * 60 * 1000L

        AlarmBroadcastReceiver.enqueuePeriodicAlarmRequest(context)

        verify {
            manager.setInexactRepeating(
                AlarmManager.RTC,
                sysCurrentTime + expectedMillis,
                AlarmManager.INTERVAL_HALF_DAY,
                pendingIntent
            )
        }
        verify { anyConstructed<Intent>().action = ACTION }
        assertThat(flagsSlot.captured and PendingIntent.FLAG_IMMUTABLE, `is`(not(0)))
    }
}
