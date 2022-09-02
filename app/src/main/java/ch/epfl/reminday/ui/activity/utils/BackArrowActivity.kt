package ch.epfl.reminday.ui.activity.utils

import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.reminday.R

/**
 * An activity that shows a back arrow in the action bar.
 * It executes the same action as pressing the physical back arrow of the phone
 * (ie. it calls [onBackPressed]).
 */
abstract class BackArrowActivity(@LayoutRes layoutRes: Int) : AppCompatActivity(layoutRes) {

    constructor() : this(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        init()
    }

    private fun init() {
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}