package com.blogspot.fdbozzo.lectorfeedsrss

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.ActivityMainBinding
import timber.log.Timber


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding: ActivityMainBinding
    //private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_main)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        navController = navHostFragment.navController
        drawerLayout = binding.drawerLayout

        /**
         * Ocultar el hamburger del NavDrawer dependiendo de si es login o no.
         */
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            // Ocultar el drawer cuando se está en ventana login
            when (destination.id) {
                R.id.nav_login -> supportActionBar?.hide()
                else -> supportActionBar?.show()
            }

        }
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_login, R.id.nav_feed_contents, R.id.nav_read_later, R.id.nav_settings
            ), drawerLayout
        )
         */

        //setupActionBarWithNavController(navController, appBarConfiguration)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)  // Para navegación y drawer
        NavigationUI.setupWithNavController(binding.navView, navController)
    }

    override fun onStart() {
        super.onStart()
        Timber.i("onStart() - fragment: %s", mainViewModel.fragmento)
    }

    /**
     *
     */
    override fun onSupportNavigateUp(): Boolean {
        //val navController = findNavController(R.id.myNavHostFragment)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        Timber.i("onSupportNavigateUp()")
        return NavigationUI.navigateUp(navController, drawerLayout)    // Activa el menu navigation del drawer
    }

    /**
     * Cerrar el drawer si se pulsa el "botón atrás" (backButton)
     */
    override fun onBackPressed() {
        Timber.i("onSupportNavigateUp()")
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


}