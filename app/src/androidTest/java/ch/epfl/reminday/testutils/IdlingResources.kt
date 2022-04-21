package ch.epfl.reminday.testutils

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource

object IdlingResources {

    private val myIdlingResources: ArrayList<IdlingResource> = arrayListOf()

    /**
     * Registers the given [IdlingResource]
     * @param resources [IdlingResources] to register into the instantiated [IdlingRegistry]
     */
    fun register(vararg resources: IdlingResource) {
        myIdlingResources.addAll(resources)
        IdlingRegistry.getInstance().register(*resources)
    }

    /**
     * Unregisters any idling resource that was added to the [IdlingRegistry].
     * Since [IdlingRegistry] is static, this method must be called after each test
     * to clean up the state and avoid bugs.
     */
    fun unregisterAll() {
        val registry = IdlingRegistry.getInstance()
        myIdlingResources.forEach { res ->
            registry.unregister(res)
        }
        myIdlingResources.clear()
    }
}