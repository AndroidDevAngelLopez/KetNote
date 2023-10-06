package com.complexsoft.ketnote.ui.screen

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.datastore.preferences.core.edit
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.ActivityMainBinding
import com.complexsoft.ketnote.ui.screen.login.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel by viewModels<MainViewModel>()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener(listener = { controller, destination, arguments ->
            if (destination.id != R.id.homeScreen) {
                binding.drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED, GravityCompat.START)
                binding.topAppBar.visibility = View.GONE
            } else {
                binding.drawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED, GravityCompat.START)
                binding.topAppBar.visibility = View.VISIBLE
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }

        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search -> {
                    true
                }

                else -> false
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.onboardingCompleted.collectLatest {
                    if (it) {
                        onBoardingComplete()
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userLoggedInComplete.collectLatest {
                    if (it) {
                        onUserLogged()
                        saveUserSettings(viewModel.userToSave)
                    }
                }
            }
        }
    }


    suspend fun saveUserSettings(userSettings: User) {
        dataStore.edit { preferences ->
            preferences[getUserDataToken] = userSettings.idToken
            preferences[getUserDataUserName] = userSettings.username
            preferences[getUserDataProfilePic] = userSettings.userImage
        }
    }

    suspend fun onUserLogged() {
        this.dataStore.edit { settings ->
            val currentValue = settings[isOnBoardingCompleted] ?: false
            settings[isOnBoardingCompleted] = !currentValue
        }
    }

    suspend fun onBoardingComplete() {
        this.dataStore.edit { settings ->
            val currentValue = settings[isUserLogged] ?: false
            settings[isUserLogged] = !currentValue
        }
    }


}