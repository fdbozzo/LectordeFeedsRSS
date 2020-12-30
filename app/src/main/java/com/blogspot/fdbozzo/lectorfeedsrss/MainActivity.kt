package com.blogspot.fdbozzo.lectorfeedsrss

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.SelectedFeedOptions
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.ActivityMainBinding
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.ui.drawer.CustomExpandableListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding: ActivityMainBinding
    //private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mainSharedViewModel: MainSharedViewModel
    //private lateinit var sharedViewModel: MainSharedViewModel
    private lateinit var mAuth: FirebaseAuth
    private var expandableListView: ExpandableListView? = null
    private var adapter: ExpandableListAdapter? = null
    internal var titleList: List<String>? = null
    private var expandableItemLongClick: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Timber.d("[Timber] onCreate() - mainSharedViewModel")
        //mainViewModel = ViewModelProvider(this).get(MainSharedViewModel::class.java)
        val localDatabase = FeedDatabase.getInstance(applicationContext)
        val feedRepository = FeedRepository(RoomDataSource(localDatabase), RssFeedDataSource())
        val sharedViewModel: MainSharedViewModel by viewModels { MainSharedViewModel.Factory(applicationContext, feedRepository) }
        //sharedViewModel = ViewModelProvider(this, MainSharedViewModel.Factory(applicationContext, feedRepository)).get(MainSharedViewModel::class.java)
        mainSharedViewModel = sharedViewModel
        sharedViewModel.testigo = "MainActivity"

        // Para que LiveData sea consciente del LifeCycle y se actualice la UI
        binding.lifecycleOwner = this

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        navController = navHostFragment.navController
        expandableListView = findViewById(R.id.expandableListView)
        drawerLayout = binding.drawerLayout
        mAuth = Firebase.auth

        /**
         * Actualizar las opciones del menú Drawer cuando se actualiza la lista de las mismas
         */
        mainSharedViewModel.menuData.observe(this, Observer {
            setupDrawerExpandableListView(it)
        })

        /**
         * Obtener feeds cuando cambia la URL seleccionada
         */
        mainSharedViewModel.apiBaseUrl.observe(this, Observer {
            it?.let {
                Timber.d("[Timber] onCreate() - mainSharedViewModel.apiBaseUrl cambiado a $it")
                drawerLayout.close()
                mainSharedViewModel.getFeeds()
            }
        })


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
         * Opción All feeds
         */
        binding.drawerMenu.navAll.setOnClickListener {
            setSelectToAllFeeds()
        }
        binding.drawerMenu.imgAll.setOnClickListener {
            setSelectToAllFeeds()
        }


        /**
         * Opción Read Later
         */
        binding.drawerMenu.imgReadLater.setOnClickListener {
            setSelectToReadLater()
        }
        binding.drawerMenu.navReadLater.setOnClickListener {
            setSelectToReadLater()
        }


        /**
         * Opción Favorites
         */
        binding.drawerMenu.imgNavFavorites.setOnClickListener {
            setSelectToFavorites()
        }
        binding.drawerMenu.navFavorites.setOnClickListener {
            setSelectToFavorites()
        }


        /**
         * Opción Logout
         */
        binding.drawerMenu.navLogout.setOnClickListener {
            logout()
        }
        binding.drawerMenu.imgLogout.setOnClickListener {
            logout()
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

    private fun setSelectToFavorites() {
        drawerLayout.close()
        Toast.makeText(this, "Favorites...", Toast.LENGTH_SHORT).show()
        mainSharedViewModel.setSelectedFeed(SelectedFeedOptions().also { it.setFavoriteTrue() })
    }

    private fun setSelectToReadLater() {
        drawerLayout.close()
        Toast.makeText(this, "Read Later...", Toast.LENGTH_SHORT).show()
        mainSharedViewModel.setSelectedFeed(SelectedFeedOptions().also { it.setReadLaterTrue() })
    }

    private fun logout() {
        mAuth.signOut()
        drawerLayout.close()
        navController.popBackStack(R.id.nav_feed_contents, true)
        navController.navigate(R.id.nav_login)
        Toast.makeText(this, "Loging out...", Toast.LENGTH_SHORT).show()
    }

    private fun setSelectToAllFeeds() {
        drawerLayout.close()
        Toast.makeText(this, "All feeds...", Toast.LENGTH_SHORT).show()
        mainSharedViewModel.setSelectedFeed(SelectedFeedOptions())
    }

    private fun setupDrawerExpandableListView(listData: HashMap<String, List<String>>) {
        //expandableListView = findViewById(R.id.expandableListView)

        if (expandableListView != null) {
            //val listData = data
            titleList = ArrayList(listData.keys)
            adapter = CustomExpandableListAdapter(this, titleList as ArrayList<String>, listData)
            expandableListView!!.setAdapter(adapter)

            // Long Click Listener
            expandableListView!!.setOnItemLongClickListener { parent, view, position, id ->
                //Timber.d("[Timber] expandableListView!!.setOnItemLongClickListener(parent:%s, view:%s, position:%d, id:%d)", parent.toString(), view.toString(), position, id)
                expandableItemLongClick = true
                false
            }


            expandableListView!!.setOnGroupExpandListener { groupPosition ->
                /*
                Toast.makeText(
                    applicationContext,
                    (titleList as ArrayList<String>)[groupPosition] + " List Expanded.",
                    Toast.LENGTH_SHORT
                ).show()
                 */
                expandableItemLongClick = false
            }

            expandableListView!!.setOnGroupCollapseListener { groupPosition ->
                /*
                Toast.makeText(
                    applicationContext,
                    (titleList as ArrayList<String>)[groupPosition] + " List Collapsed.",
                    Toast.LENGTH_SHORT
                ).show()
                 */
                expandableItemLongClick = false
            }

            expandableListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
                if (expandableItemLongClick) {
                    Timber.d("[Timber] expandableListView!!.setOnChildClickListener(LONG CLICK!)")
                    expandableItemLongClick = false
                    return@setOnChildClickListener true
                } else {
                    Timber.d(
                        "[Timber] expandableListView!!.setOnChildClickListener(groupPosition = %d, childPosition = %d)",
                        groupPosition,
                        childPosition
                    )

                    val linkName = listData[(titleList as ArrayList<String>)[groupPosition]]!![childPosition]
                    mainSharedViewModel.getFeedWithLinkNameAndSetApiBaseUrl(linkName)

                    /*
                    Toast.makeText(
                        applicationContext,
                        "Child Clicked: " +
                                (titleList as ArrayList<String>)[groupPosition] + " -> " +
                                listData[(titleList as ArrayList<String>)[groupPosition]]!![childPosition],
                        Toast.LENGTH_SHORT
                    ).show()
                     */

                    expandableItemLongClick = false
                }
                false
            }

            expandableListView!!.setOnGroupClickListener { parent, v, groupPosition, id ->
                if (expandableItemLongClick) {
                    Timber.d("[Timber] expandableListView!!.setOnGroupClickListener(LONG CLICK!)")
                    expandableItemLongClick = false
                    return@setOnGroupClickListener true
                } else {
                    Timber.d(
                        "[Timber] expandableListView!!.setOnGroupClickListener(groupPosition = %d)",
                        groupPosition
                    )
                    setListViewHeight(parent, groupPosition)
                    /*
                    Toast.makeText(
                        applicationContext,
                        "Group Clicked: " +
                                (titleList as ArrayList<String>)[groupPosition],
                        Toast.LENGTH_SHORT
                    ).show()
                     */

                    expandableItemLongClick = false
                }
                false
            }
        }
    }



    override fun onStart() {
        super.onStart()
        Timber.d("[Timber] onStart() - mainSharedViewModel.fragmento: %s", mainSharedViewModel.testigo)
        mainSharedViewModel.testigo = MainActivity::class.java.canonicalName
    }

    /**
     * Soporte para la opción de navegación "atrás"
     */
    override fun onSupportNavigateUp(): Boolean {
        //val navController = findNavController(R.id.myNavHostFragment)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        Timber.d("[Timber] onSupportNavigateUp()")
        return NavigationUI.navigateUp(
            navController,
            drawerLayout
        )    // Activa el menu navigation del drawer
    }

    /**
     * Cerrar el drawer si se pulsa el "botón atrás" (backButton)
     */
    override fun onBackPressed() {
        Timber.d("[Timber] onBackPressed()")
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    //----- ESTE MENU SUPERIOR/DERECHO FUNCIONA, PERO NO LE VEO SENTIDO TENIENDO EL MENU DEL DRAWER -----
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