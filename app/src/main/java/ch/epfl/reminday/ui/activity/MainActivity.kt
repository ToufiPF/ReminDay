package ch.epfl.reminday.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.Birthday
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.databinding.ActivityMainBinding
import ch.epfl.reminday.ui.fragment.BirthdayListFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.MonthDay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var dao: BirthdayDao

    private lateinit var binding: ActivityMainBinding

    private lateinit var addBirthdayItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            dao.insertAll(Birthday("Toufi", MonthDay.of(3, 16)))
        }

        supportFragmentManager.beginTransaction()
            .add(binding.container.id, BirthdayListFragment())
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)

        menu?.let {
            addBirthdayItem = menu.findItem(R.id.add_birthday_item)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_birthday_item -> {
                launchAddBirthdayActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun launchAddBirthdayActivity() {
        val intent = Intent(this, AddBirthdayActivity::class.java)
        startActivity(intent)
    }
}