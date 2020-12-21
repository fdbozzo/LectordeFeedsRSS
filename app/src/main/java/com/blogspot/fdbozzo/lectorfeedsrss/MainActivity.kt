package com.blogspot.fdbozzo.lectorfeedsrss

import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.ActivityMainBinding
import com.blogspot.fdbozzo.lectorfeedsrss.ui.drawer.CustomExpandableListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mainViewModel: MainViewModel
    private lateinit var mAuth: FirebaseAuth
    private var expandableListView: ExpandableListView? = null
    private var adapter: ExpandableListAdapter? = null
    internal var titleList: List<String>? = null

    val data: HashMap<String, List<String>>
        get() {
            val listData = HashMap<String, List<String>>()

            val redmiMobiles = ArrayList<String>()
            redmiMobiles.add("Redmi Y2")
            redmiMobiles.add("Redmi S2")
            redmiMobiles.add("Redmi Note 5 Pro")
            redmiMobiles.add("Redmi Note 5")
            redmiMobiles.add("Redmi 5 Plus")
            redmiMobiles.add("Redmi Y1")
            redmiMobiles.add("Redmi 3S Plus")

            val micromaxMobiles = ArrayList<String>()
            micromaxMobiles.add("Micromax Bharat Go")
            micromaxMobiles.add("Micromax Bharat 5 Pro")
            micromaxMobiles.add("Micromax Bharat 5")
            micromaxMobiles.add("Micromax Canvas 1")
            micromaxMobiles.add("Micromax Dual 5")

            val appleMobiles = ArrayList<String>()
            appleMobiles.add("iPhone 8")
            appleMobiles.add("iPhone 8 Plus")
            appleMobiles.add("iPhone X")
            appleMobiles.add("iPhone 7 Plus")
            appleMobiles.add("iPhone 7")
            appleMobiles.add("iPhone 6 Plus")

            val samsungMobiles = ArrayList<String>()
            samsungMobiles.add("Samsung Galaxy S9+")
            samsungMobiles.add("Samsung Galaxy Note 7")
            samsungMobiles.add("Samsung Galaxy Note 5 Dual")
            samsungMobiles.add("Samsung Galaxy S8")
            samsungMobiles.add("Samsung Galaxy A8")
            samsungMobiles.add("Samsung Galaxy Note 4")

            listData["Redmi"] = redmiMobiles
            listData["Micromax"] = micromaxMobiles
            listData["Apple"] = appleMobiles
            listData["Samsung"] = samsungMobiles

            return listData
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_main)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.lifecycleOwner =
            this // Para que LiveData sea consciente del LifeCycle y se actualice la uI

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        navController = navHostFragment.navController
        drawerLayout = binding.drawerLayout
        mAuth = Firebase.auth

        setupDrawerExpandableListView(data)

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

        /**
         * Logout
         */
        binding.drawerMenu.navLogout.setOnClickListener {
            mAuth.signOut()
            drawerLayout.close()
            navController.popBackStack(R.id.nav_feed_contents, true)
            navController.navigate(R.id.nav_login)
            Toast.makeText(this, "Loging out...", Toast.LENGTH_SHORT).show()
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
        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            drawerLayout
        )  // Para navegación y drawer
        //NavigationUI.setupWithNavController(binding.navView, navController)

    }

    private fun setupDrawerExpandableListView(listData: HashMap<String, List<String>>) {
        expandableListView = findViewById(R.id.expandableListView)

        if (expandableListView != null) {
            //val listData = data
            titleList = ArrayList(listData.keys)
            adapter = CustomExpandableListAdapter(this, titleList as ArrayList<String>, listData)
            expandableListView!!.setAdapter(adapter)

            expandableListView!!.setOnGroupExpandListener { groupPosition ->
                Toast.makeText(
                    applicationContext,
                    (titleList as ArrayList<String>)[groupPosition] + " List Expanded.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            expandableListView!!.setOnGroupCollapseListener { groupPosition ->
                Toast.makeText(
                    applicationContext,
                    (titleList as ArrayList<String>)[groupPosition] + " List Collapsed.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            expandableListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
                Toast.makeText(
                    applicationContext,
                    "Clicked: " +
                            (titleList as ArrayList<String>)[groupPosition] + " -> " +
                            listData[(titleList as ArrayList<String>)[groupPosition]]!![childPosition],
                    Toast.LENGTH_SHORT
                ).show()
                false
            }

            expandableListView!!.setOnGroupClickListener { parent, v, groupPosition, id ->
                setListViewHeight(parent, groupPosition)
                false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Timber.i("onStart() - fragment: %s", mainViewModel.fragmento)
    }

    /**
     * Soporte para la opción de navegación "atrás"
     */
    override fun onSupportNavigateUp(): Boolean {
        //val navController = findNavController(R.id.myNavHostFragment)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        Timber.i("onSupportNavigateUp()")
        return NavigationUI.navigateUp(
            navController,
            drawerLayout
        )    // Activa el menu navigation del drawer
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

    //----- ESTE MENU FUNCIONA, PERO NO LE VEO SENTIDO TENIENDO EL MENU DEL DRAWER -----
    /*
    // MENU PRINCIPAL
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.navdrawer_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Opción de menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {
                // TODO
                Toast.makeText(this, "Loging out...", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
     */

    /**
     * Este método resuelve el problema de que el ExpandableListView no respete los controles
     * que hay arriba o abajo del mismo, dejándolos fuera de vista.
     *
     * Fuente: https://www.developerlibs.com/2020/10/android-expandable-listview-inside-scrollview.html
     */
    private fun setListViewHeight(
        listView: ExpandableListView,
        group: Int
    ) {
        val listAdapter = listView.expandableListAdapter as ExpandableListAdapter
        var totalHeight = 0
        val desiredWidth: Int = View.MeasureSpec.makeMeasureSpec(
            listView.width,
            View.MeasureSpec.EXACTLY
        )
        for (i in 0 until listAdapter.groupCount) {
            val groupItem: View = listAdapter.getGroupView(i, false, null, listView)
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
            totalHeight += groupItem.measuredHeight
            if (listView.isGroupExpanded(i) && group != i
                || !listView.isGroupExpanded(i) && group == i
            ) {
                for (j in 0 until listAdapter.getChildrenCount(i)) {
                    val listItem: View = listAdapter.getChildView(
                        i, j, false, null,
                        listView
                    )
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
                    totalHeight += listItem.measuredHeight
                }
            }
        }
        val params = listView.layoutParams
        var height = (totalHeight
                + listView.dividerHeight * (listAdapter.groupCount - 1))
        if (height < 10) height = 200
        params.height = height
        listView.layoutParams = params
        listView.requestLayout()
    }
}