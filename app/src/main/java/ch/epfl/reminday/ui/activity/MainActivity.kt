package ch.epfl.reminday.ui.activity

import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.test.espresso.idling.CountingIdlingResource
import ch.epfl.reminday.R
import ch.epfl.reminday.data.birthday.BirthdayDao
import ch.epfl.reminday.databinding.ActivityMainBinding
import ch.epfl.reminday.ui.fragment.BirthdayListFragment
import ch.epfl.reminday.util.Extensions.showConfirmationDialog
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

    val importIdlingResource = CountingIdlingResource("import_contacts")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragment: BirthdayListFragment = binding.birthdayListFragment.getFragment()
        fragment.validateAuthentication()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
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

    private fun launchAddBirthdayActivity() {
        val intent = Intent(this, BirthdayEditActivity::class.java)
        intent.putExtra(BIRTHDAY_EDIT_MODE_ORDINAL, BirthdayEditActivity.Mode.ADD.ordinal)
        startActivity(intent)
    }

    private fun importBirthdaysFromContacts() {
        if (viewModel.mayRequireContacts(this)) {
            showConfirmationDialog(
                title = R.string.are_you_sure,
                text = R.string.import_from_contacts_are_you_sure,
                onConfirm = this::doImport
            )
        }
    }

    private fun doImport() {
        importIdlingResource.increment()

        lifecycleScope.launch {
            val contacts = viewModel.importContacts()

            if (contacts.isEmpty()) {
                Log.d(this@MainActivity.javaClass.name, "Found no contacts to import")
                Toast.makeText(
                    this@MainActivity,
                    R.string.import_from_contacts_no_contacts,
                    Toast.LENGTH_LONG
                ).show()
            } else {
                dao.insertAll(*contacts.toTypedArray())
            }

            importIdlingResource.decrement()
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