package ch.epfl.reminday.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Base64
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import ch.epfl.reminday.Reflection
import ch.epfl.reminday.util.constant.PreferenceNames.EncryptedPreferenceNames.DATABASE_KEY
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

class DatabaseKeyManagerImplTest {

    companion object {
        private val BUILD_VERSION = Build.VERSION.SDK_INT

        private val buildVersionField: Field
            get() = Build.VERSION::class.java.getField("SDK_INT")

        @AfterClass
        @JvmStatic
        fun cleanUpClass() {
            Reflection.setFinalStaticField(buildVersionField, BUILD_VERSION)
        }
    }

    private lateinit var context: Context
    private lateinit var keyMgr: DatabaseKeyManager
    private lateinit var preferences: SharedPreferences

    @Before
    fun init() {
        Reflection.setFinalStaticField(buildVersionField, BUILD_VERSION)

        context = getApplicationContext()

        DatabaseKeyManagerImpl(context).let { mgr ->
            keyMgr = mgr
            preferences = mgr.openEncryptedPreferences()
        }
        preferences.edit().clear().apply()
    }

    @After
    fun clear() {
        preferences.edit().clear().apply()
    }

    @Test
    fun newKeyDoesGenerateAKey() {
        assertNotNull(keyMgr.newDatabaseKey())
    }

    @Test
    fun isEncryptionSupportedReturnsValueDependingOnAPI() {
        Reflection.setFinalStaticField(buildVersionField, 22)
        assertFalse(keyMgr.isDatabaseEncryptionSupported())

        Reflection.setFinalStaticField(buildVersionField, 23)
        assertTrue(keyMgr.isDatabaseEncryptionSupported())
    }

    @Test
    fun loadKeyGeneratesNewKeyIfNoKeyStoredYet() {
        assertNotNull(keyMgr.loadDatabaseKey())
    }

    @Test
    fun loadKeyReturnsStoredKey() {
        val key = ByteArray(32) { (it * 2).toByte() }
        val base64Key = Base64.encodeToString(key, Base64.DEFAULT)

        preferences.edit()
            .putString(DATABASE_KEY, base64Key)
            .apply()

        assertArrayEquals(key, keyMgr.loadDatabaseKey())
    }

    @Test
    fun storeKeyDoesStoreTheKeyToPreferences() {
        val key = ByteArray(32) { (it * 2).toByte() }
        val base64Key = Base64.encodeToString(key, Base64.DEFAULT)

        preferences.edit()
            .putString(DATABASE_KEY, "falseKey")
            .apply()

        keyMgr.storeDatabaseKey(key)
        assertEquals(base64Key, preferences.getString(DATABASE_KEY, null))

        keyMgr.storeDatabaseKey(null)
        assertNull(preferences.getString(DATABASE_KEY, null))
    }

    @Test
    fun keyManagerThrowExceptionsIfAPINotSupported() {
        Reflection.setFinalStaticField(buildVersionField, 19)

        val key = ByteArray(32) { (it * 2).toByte() }
        assertThrows(IllegalStateException::class.java) {
            keyMgr.storeDatabaseKey(key)
        }

        assertThrows(IllegalStateException::class.java) {
            keyMgr.loadDatabaseKey()
        }
    }
}
