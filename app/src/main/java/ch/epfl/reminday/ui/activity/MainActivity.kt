package ch.epfl.reminday.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ch.epfl.reminday.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}