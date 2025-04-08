package com.mcsor.sprintfield

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.ui.AppBarConfiguration
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navView.setNavigationItemSelectedListener { menuItem ->
            val handled = when (menuItem.itemId) {
                R.id.homeFragment -> {
                    navController.popBackStack(R.id.homeFragment, false)
                    true
                }
                else -> NavigationUI.onNavDestinationSelected(menuItem, navController)
            }
            drawerLayout.closeDrawers()
            handled
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.settingsFragment, R.id.tasksFragment, R.id.sprintsFragment, R.id.projectsFragment, R.id.teamsFragment), drawerLayout
        )

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }
}
