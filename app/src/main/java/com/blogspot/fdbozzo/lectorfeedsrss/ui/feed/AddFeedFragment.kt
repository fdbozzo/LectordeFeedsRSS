package com.blogspot.fdbozzo.lectorfeedsrss.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.blogspot.fdbozzo.lectorfeedsrss.MainSharedViewModel
import com.blogspot.fdbozzo.lectorfeedsrss.R
import com.blogspot.fdbozzo.lectorfeedsrss.data.RssResponse
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.feed.Group as RoomGroup
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Feed
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.feed.Group
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.AddFeedFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.ui.SealedClassAppScreens
import com.blogspot.fdbozzo.lectorfeedsrss.util.forceEndingWithChar
import com.blogspot.fdbozzo.lectorfeedsrss.util.forceNotEndingWithString
import com.blogspot.fdbozzo.lectorfeedsrss.util.forceStartingWithString
import com.blogspot.fdbozzo.lectorfeedsrss.util.hideKeyboard
import kotlinx.coroutines.launch
import timber.log.Timber

class AddFeedFragment : Fragment() {

    companion object {
        fun newInstance() = AddFeedFragment()
    }

    private var _binding: AddFeedFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //private lateinit var viewModel: AddFeedViewModel
    private lateinit var mainSharedViewModel: MainSharedViewModel
    private lateinit var navController: NavController
    private var groupList: List<Group>? = null
    private var selectedGroup: Group? = null
    private var groupNameList: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddFeedFragmentBinding.inflate(inflater, container, false)

        val localDatabase = FeedDatabase.getInstance(requireContext())
        val feedRepository = FeedRepository(RoomDataSource(localDatabase), RssFeedDataSource())
        val sharedViewModel: MainSharedViewModel by activityViewModels {
            MainSharedViewModel.Factory(
                feedRepository
            )
        }
        mainSharedViewModel = sharedViewModel
        mainSharedViewModel.setActiveScreen(SealedClassAppScreens.AddFeedFragment)
        //viewModel = ViewModelProvider(this).get(AddFeedViewModel::class.java)

        Timber.i(
            "[Timber] onCreateView() - mainSharedViewModel.fragmento: %s",
            mainSharedViewModel.testigo
        )
        mainSharedViewModel.testigo = AddFeedFragment::class.java.canonicalName

        binding.lifecycleOwner =
            this // Para que LiveData sea consciente del LifeCycle y se actualice la uI
        //binding.viewModel = viewModel
        binding.fragment = this

        navController = findNavController()

        lifecycleScope.launch { prepararDatosAutoCompleteTextView() }

        /**
         * Listener para el item elegido en el Spinner de grupos
         */
        binding.spinnerDropDown.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (groupList != null) {
                        selectedGroup = groupList?.get(position)!!
                        Timber.d(
                            "[Timber] selectedGroup = %s, id=%d",
                            selectedGroup!!.groupName,
                            selectedGroup!!.id
                        )
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // TODO("Not yet implemented")
                }
            }

        return binding.root
        //return inflater.inflate(R.layout.add_feed_fragment, container, false)
    }

    private suspend fun prepararDatosAutoCompleteTextView() {
        // Obtener los grupos
        groupList = mainSharedViewModel.feedRepository.getGroups()

        if (groupList != null) {
            // Convertir la lista de Grupos en lista de sus nombres
            groupNameList = groupList!!.map {
                it.groupName
            }

            // Preparar un adaptador con esa lista de nombres
            val listAdpater = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                groupNameList
            )

            // Asignársela al autoCompleteTextView
            binding.spinnerDropDown.adapter = listAdpater

            // Busco la posición del item "UNCATEGORIZED"
            val index = listAdpater.getPosition(RoomGroup.DEFAULT_NAME)

            // Si existe, lo configuro por defecto
            if (index >= 0) {
                binding.spinnerDropDown.setSelection(index)
            }
        }
    }


    fun clickAdd() {
        hideKeyboard()
        val link = binding.editLink.text.toString()
        Timber.d("[Timber] Cick! %s", link)

        if (link.isNotBlank() && selectedGroup != null) {
            binding.editLink.text?.clear()

            // Se busca el Feed para comprobar si es válido y lo intenta guardar, si no existe
            lifecycleScope.launch { buscarFeedComprobarSiEsValidoYGuardar(link, selectedGroup!!) }

        } else {
            binding.textInputLink.error = getString(R.string.err_url_cant_be_empty)
            //binding.editLink.requestFocus()
        }
    }

    /**
     * Se busca el Feed para comprobar si es válido y lo intenta guardar, si no existe
     */
    private suspend fun buscarFeedComprobarSiEsValidoYGuardar(link: String, group: Group) {
        try {
            binding.textInputLink.error = ""
            val rssApiResponse = mainSharedViewModel.buscarFeedComprobarSiEsValidoYGuardar(link, group)

            /*
            when (rssApiResponse) {
                is RssResponse.Success -> {
                    // OK (ya se trató en el ViewModel)
                }
                /* Los errores de comunicaciones los debe tratar el ViewModel. No se muestran en el textInput.
                is RssResponse.Error -> {
                    // Mostrar mensaje error
                    Timber.d((rssApiResponse as RssResponse.Error).exception, "[Timber] AddFeedFragment.buscarFeedObtenerInfoYGuardar --> RssResponse.Error")
                    Timber.d("[Timber] AddFeedFragment.buscarFeedObtenerInfoYGuardar --> RssResponse.Error = ${(rssApiResponse as RssResponse.Error).exception.message}")
                    //throw Exception((rssApiResponse as RssResponse.Error).exception.message)
                    binding.textInputLink.error = (rssApiResponse as RssResponse.Error).exception.message
                }
                 */
            }
             */

        } catch (e: Exception) {
            // Mostrar mensaje error
            Timber.d(e, "[Timber] AddFeedFragment.buscarFeedObtenerInfoYGuardar --> Error")
            binding.textInputLink.error = e.message
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        //binding.editLink.requestFocus()
    }
}