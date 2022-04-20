package ch.epfl.reminday.ui.activity

import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.databinding.ActivityMainBinding
import ch.epfl.reminday.util.constant.ArgumentNames.BIRTHDAY_EDIT_MODE_ORDINAL
import ch.epfl.reminday.viewmodel.activity.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var dao: BirthdayDao

    private val viewModel: MainViewModel by viewModels()
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
            R.id.import_from_contacts_item -> {
                importBirthdaysFromContacts()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun launchAddBirthdayActivity() {
        val intent = Intent(this, BirthdayEditActivity::class.java)
        intent.putExtra(BIRTHDAY_EDIT_MODE_ORDINAL, BirthdayEditActivity.Mode.ADD.ordinal)
        startActivity(intent)
    }

    private fun importBirthdaysFromContacts() {
        if (viewModel.mayRequireContacts(this)) {
            lifecycleScope.launch {
                val contacts = viewModel.importContacts(this@MainActivity)
                contacts.forEach { Log.d(this@MainActivity.javaClass.name, it.toString()) }
                dao.insertAll(*contacts.toTypedArray())
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MainViewModel.READ_CONTACTS_PERMISSION_CODE -> {
                // try again to import if permission was granted
                if (grantResults.all { it == PERMISSION_GRANTED }) {
                    importBirthdaysFromContacts()
                }
            }
            else ->
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}