package ch.epfl.reminday.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
}