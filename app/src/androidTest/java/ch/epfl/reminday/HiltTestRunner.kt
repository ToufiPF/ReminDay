package ch.epfl.reminday

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.multidex.MultiDex
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }

    override fun onCreate(arguments: Bundle?) {
        MultiDex.install(targetContext)
        super.onCreate(arguments)
    }
}