package ch.epfl.reminday.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var dao: BirthdayDao

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
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
        val intent = Intent(this, BirthdayEditActivity::class.java)
        startActivity(intent)
    }
}