package com.complexsoft.ketnote.ui.screen

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.complexsoft.ketnote.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewModel by viewModels<MainViewModel>()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.onboardingCompleted.collectLatest {
                    if (it) {
                        onBoardingComplete()
                    }
                }
            }
        }

    }


    suspend fun onBoardingComplete() {
        this.dataStore.edit { settings ->
            val currentValue = settings[isOnBoardingCompleted] ?: false
            settings[isOnBoardingCompleted] = !currentValue
        }
    }


}