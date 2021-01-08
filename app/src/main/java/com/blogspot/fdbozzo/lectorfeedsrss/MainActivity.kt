package com.blogspot.fdbozzo.lectorfeedsrss

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.SelectedFeedOptions
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.ActivityMainBinding
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.ui.drawer.CustomExpandableListAdapter
import com.blogspot.fdbozzo.lectorfeedsrss.ui.main.BottomSheetFeedOptionsMenuFragment
import com.blogspot.fdbozzo.lectorfeedsrss.ui.main.BottomSheetGroupOptionsMenuFragment
import com.blogspot.fdbozzo.lectorfeedsrss.ui.main.BottomSheetMarkAsReadOptionsMenuFragment
import com.blogspot.fdbozzo.lectorfeedsrss.ui.SealedClassAppScreens
import com.blogspot.fdbozzo.lectorfeedsrss.util.toBoolean
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding: ActivityMainBinding

    //private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mainSharedViewModel: MainSharedViewModel
    private lateinit var mAuth: FirebaseAuth
    private var expandableListView: ExpandableListView? = null
    private var adapter: ExpandableListAdapter? = null
    internal var titleList: List<String>? = null
    private var expandableItemLongClick: Boolean = false
    private lateinit var sharedPreferences: SharedPreferences
    var showUnreadOnlyPref: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Guarda las preferencias por única vez, si no estaban ya guardadas
        PreferenceManager.setDefaultValues(applicationContext, R.xml.preferences, false)

        // Lee las preferencias
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        Timber.d("[Timber] onCreate() - mainSharedViewModel")
        val localDatabase = FeedDatabase.getInstance(applicationContext)
        val feedRepository = FeedRepository(RoomDataSource(localDatabase), RssFeedDataSource())
        val sharedViewModel: MainSharedViewModel by viewModels {
            MainSharedViewModel.Factory(
                feedRepository
            )
        }
        mainSharedViewModel = sharedViewModel
        sharedViewModel.testigo = "MainActivity"
        mainSharedViewModel.setActiveScreen(SealedClassAppScreens.MainActivity)

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

                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START)

                mainSharedViewModel.getFeeds()
            }
        })

        mainSharedViewModel.selectedFeedChannelItemId.observe(this, Observer {
            Timber.d("[Timber] (Observer) selectedFeedChannelItemId= %d", it)
        })

        mainSharedViewModel.autoUpdatedSelectedFeedChannelItemWithFeed.observe(this, Observer {
            Timber.d(
                "[Timber] (Observer) autoUpdatedSelectedFeedChannelItemWithFeed(%d) = %s",
                it.id,
                it.link
            )
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


        /**
         * Opción Settings
         */
        binding.drawerMenu.imgNavSettings.setOnClickListener {
            navigateToSettings()
        }
        binding.drawerMenu.navSettings.setOnClickListener {
            navigateToSettings()
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


        /**
         * Cargar menú superior según el fragmento cargado
         */
        mainSharedViewModel.selectedScreen.observe(this, Observer {
            binding.topAppBar.menu.clear()

            // Carga preferencia para "show_unread_only"
            showUnreadOnlyPref =
                sharedPreferences.getBoolean(getString(R.string.pref_show_unread_only), false)
            Timber.d("[Timber] selectedScreen.observe: showUnreadOnlyPref = %s", showUnreadOnlyPref)

            when (it) {
                is SealedClassAppScreens.MainActivity -> {
                    Timber.d("[Timber] Menu MainActivity")
                }
                is SealedClassAppScreens.FeedChannelFragment -> {
                    Timber.d("[Timber] Menu FeedChannelFragment")
                    binding.topAppBar.inflateMenu(R.menu.upper_navdrawer_feedchannel_menu)

                    val feedOptionsValue = mainSharedViewModel.selectedFeedOptions.value

                    if (feedOptionsValue != null) {
                        // Actualizo el título de la ventana de acuerdo a la selección de opciones activas
                        when {
                            feedOptionsValue.readLater -> {
                                binding.topAppBar.setTitle(R.string.screen_title_read_later)
                            }
                            feedOptionsValue.favorite -> {
                                binding.topAppBar.setTitle(R.string.screen_title_favorites)
                            }
                            feedOptionsValue.linkName != "%" -> {
                                binding.topAppBar.title = feedOptionsValue.linkName
                            }
                            else -> {
                                binding.topAppBar.setTitle(R.string.screen_title_all_feeds)
                            }
                        }
                    }


                    // Actualiza el filtro con el valor del setting global de "showUnreadOnly"
                    mainSharedViewModel.setSelectedFeedOptionsReadFlag(!showUnreadOnlyPref)
                }
                is SealedClassAppScreens.ContentsFragment -> {
                    Timber.d("[Timber] Menu ContentsFragment")
                    binding.topAppBar.inflateMenu(R.menu.upper_navdrawer_feedchannelitem_menu)
                    binding.topAppBar.title = ""
                }
                is SealedClassAppScreens.SettingsFragment -> {
                    Timber.d("[Timber] Menu SettingsFragment")
                    binding.topAppBar.setTitle(R.string.screen_title_settings)
                }
                else -> Unit
            }
        })

        /**
         * Controlar las opciones elegidas del menú superior
         */
        binding.topAppBar.setOnMenuItemClickListener {

            when (it.itemId) {
                R.id.nav_mark_all_as_read -> {
                    val tituloMenu = "Mark as read"
                    BottomSheetMarkAsReadOptionsMenuFragment(tituloMenu).show(
                        supportFragmentManager,
                        "submenu_all_feeds"
                    )
                }
                R.id.nav_mark_as_unread -> {
                    Timber.d("[Timber] Mark as unread")
                    sharedViewModel.updateItemReadStatus(false)
                    navController.popBackStack()
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.msg_marked_as_unread),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                R.id.nav_mark_as_read_later -> {
                    Timber.d("[Timber] Change read later status")
                    sharedViewModel.updateItemReadLaterStatus()
                    navController.popBackStack()
                }
                R.id.nav_settings -> {
                    Timber.d("[Timber] Settings")
                    navigateToSettings()
                }
                else -> {
                    Timber.d(
                        "[Timber] topAppBar.setOnMenuItemClickListener(%s)",
                        it.itemId.toString()
                    )
                }
            }
            false
        }

        /**
         * Activar el menú del ActionBar (inferior)
         */
        setSupportActionBar(binding.bottomAppBar)

        //setupActionBarWithNavController(navController, appBarConfiguration)
        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            drawerLayout
        )  // Para navegación y drawer
        //NavigationUI.setupWithNavController(binding.navView, navController)

    }

    private fun setSelectToFavorites() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)

        Toast.makeText(this, "Favorites...", Toast.LENGTH_SHORT).show()
        mainSharedViewModel.setSelectedFeedOptions(SelectedFeedOptions().also { it.setFavoriteTrue() })
        binding.topAppBar.setTitle(R.string.screen_title_favorites)
    }

    private fun setSelectToReadLater() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)

        Toast.makeText(this, "Read Later...", Toast.LENGTH_SHORT).show()
        mainSharedViewModel.setSelectedFeedOptions(SelectedFeedOptions().also { it.setReadLaterTrue() })
        binding.topAppBar.setTitle(R.string.screen_title_read_later)
    }

    private fun logout() {
        mAuth.signOut()

        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)

        navController.popBackStack(R.id.nav_feed_contents, true)
        navController.navigate(R.id.nav_login)
        Toast.makeText(this, "Loging out...", Toast.LENGTH_SHORT).show()
    }

    private fun setSelectToAllFeeds() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)

        Toast.makeText(this, "All feeds...", Toast.LENGTH_SHORT).show()
        mainSharedViewModel.setSelectedFeedOptions(SelectedFeedOptions())
        binding.topAppBar.setTitle(R.string.screen_title_all_feeds)
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
                expandableItemLongClick = false
            }

            expandableListView!!.setOnGroupCollapseListener { groupPosition ->
                expandableItemLongClick = false
            }

            /**
             * Controlador de selección (click) de Feed para mostrar sus noticias
             */
            expandableListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
                if (expandableItemLongClick) {
                    // LONG-CLICK
                    //lifecycleScope.launch {
                        Timber.d("[Timber] expandableListView!!.setOnChildClickListener(LONG CLICK!)")
                        expandableItemLongClick = false

                        val tituloMenu =
                            listData[(titleList as ArrayList<String>)[groupPosition]]!![childPosition]

                    lifecycleScope.launch {
                        val feed = mainSharedViewModel.getFeedWithLinkName(tituloMenu)
                        Timber.d("[Timber] expandableListView!!.setOnChildClickListener(LONG CLICK!) -> Feed encontrado: %s, favorite=%d",
                            feed, feed.favorite)

                        // Cargar el menú
                        BottomSheetFeedOptionsMenuFragment(tituloMenu, feed).show(
                            supportFragmentManager,
                            "submenu"
                        )
                    }

                    //}

                    return@setOnChildClickListener true
                } else {
                    // CLICK NORMAL
                    Timber.d(
                        "[Timber] expandableListView!!.setOnChildClickListener(groupPosition = %d, childPosition = %d)",
                        groupPosition,
                        childPosition
                    )

                    val linkName =
                        listData[(titleList as ArrayList<String>)[groupPosition]]!![childPosition]
                    mainSharedViewModel.getFeedWithLinkNameAndSetApiBaseUrl(linkName)
                    binding.topAppBar.title = linkName

                    expandableItemLongClick = false
                }
                false
            }

            /**
             * Controlador de selección (click) de Grupo para mostrar sus feeds
             */
            expandableListView!!.setOnGroupClickListener { parent, v, groupPosition, id ->
                if (expandableItemLongClick) {
                    Timber.d("[Timber] expandableListView!!.setOnGroupClickListener(LONG CLICK!)")
                    expandableItemLongClick = false

                    val tituloMenu = (titleList as ArrayList<String>)[groupPosition]

                    BottomSheetGroupOptionsMenuFragment(tituloMenu).show(
                        supportFragmentManager,
                        "submenu"
                    )

                    return@setOnGroupClickListener true
                } else {
                    Timber.d(
                        "[Timber] expandableListView!!.setOnGroupClickListener(groupPosition = %d)",
                        groupPosition
                    )
                    setListViewHeight(parent, groupPosition)

                    expandableItemLongClick = false
                }
                false
            }
        }
    }


    override fun onStart() {
        super.onStart()
        Timber.d(
            "[Timber] onStart() - mainSharedViewModel.fragmento: %s",
            mainSharedViewModel.testigo
        )
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
    //*
    // MENU PRINCIPAL
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //menuInflater.inflate(R.menu.navdrawer_menu, menu)
        Timber.d("[Timber] onCreateOptionsMenu()")
        menuInflater.inflate(R.menu.bottom_nav_menu, menu)
        return true // super.onCreateOptionsMenu(menu)
    }

    // Opción de menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Timber.d("[Timber] onOptionsItemSelected()")
        when (item.itemId) {
            R.id.nav_logout -> {
                logout()
                Toast.makeText(this, "Logged out.", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_settings -> {
                navigateToSettings()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToSettings() {
        binding.topAppBar.menu.clear()

        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)

        navController.navigate(R.id.nav_settings)
    }
    //*/

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