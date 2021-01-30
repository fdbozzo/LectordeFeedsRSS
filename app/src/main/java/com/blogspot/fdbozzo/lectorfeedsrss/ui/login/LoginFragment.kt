package com.blogspot.fdbozzo.lectorfeedsrss.ui.login

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.blogspot.fdbozzo.lectorfeedsrss.MainSharedViewModel
import com.blogspot.fdbozzo.lectorfeedsrss.R
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.FeedDatabase
import com.blogspot.fdbozzo.lectorfeedsrss.data.database.RoomDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.data.domain.FeedRepository
import com.blogspot.fdbozzo.lectorfeedsrss.databinding.LoginFragmentBinding
import com.blogspot.fdbozzo.lectorfeedsrss.network.RssFeedDataSource
import com.blogspot.fdbozzo.lectorfeedsrss.ui.SealedClassAppScreens
import com.blogspot.fdbozzo.lectorfeedsrss.util.hideKeyboard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class LoginFragment : Fragment(), View.OnClickListener {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private var _binding: LoginFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mAuth: FirebaseAuth
    private lateinit var viewModel: LoginViewModel
    private lateinit var emailUsuario: EditText
    private lateinit var passUsuario: EditText
    private lateinit var btnLogin: Button
    private lateinit var lnkSignup: TextView
    private lateinit var navController: NavController
    //private lateinit var navGraph: NavGraph
    private lateinit var mainSharedViewModel: MainSharedViewModel
    //private lateinit var sharedViewModel: MainSharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        _binding = LoginFragmentBinding.inflate(inflater, container, false)

        //mainSharedViewModel = ViewModelProvider(requireActivity()).get(MainSharedViewModel::class.java)
        val localDatabase = FeedDatabase.getInstance(requireContext())
        val feedRepository = FeedRepository(RoomDataSource(localDatabase), RssFeedDataSource())
        val sharedViewModel: MainSharedViewModel by activityViewModels { MainSharedViewModel.Factory(feedRepository) }
        //sharedViewModel = ViewModelProvider(this, MainSharedViewModel.Factory(requireContext(), feedRepository)).get(MainSharedViewModel::class.java)
        mainSharedViewModel = sharedViewModel
        mainSharedViewModel.setActiveScreen(SealedClassAppScreens.LoginFragment)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        Timber.i("onCreateView() - mainSharedViewModel.fragmento: %s", mainSharedViewModel.testigo)
        mainSharedViewModel.testigo = LoginFragment::class.java.canonicalName

        binding.lifecycleOwner = this // Para que LiveData sea consciente del LifeCycle y se actualice la uI
        mAuth = Firebase.auth

        navController = findNavController()

        /*
        val navHostFragment =
            parentFragmentManager.findFragmentById(R.id.fragmentContainerView)

        if (navHostFragment != null) {
            navController = navHostFragment.findNavController()
            //val graphInflater = navController.navInflater
            //navGraph = graphInflater.inflate(R.navigation.nav_graph)
        }
         */

        /**
         * Datos de login
         */
        emailUsuario = binding.inputEmail // requireActivity().findViewById(R.id.input_email)
        passUsuario =
            binding.inputPassword // requireActivity().findViewById(R.id.input_password)
        btnLogin = binding.btnLogin // requireActivity().findViewById(R.id.btn_login)
        lnkSignup = binding.linkSignup // requireActivity().findViewById(R.id.link_signup)

        btnLogin.setOnClickListener(this)
        lnkSignup.setOnClickListener(this)

        //return inflater.inflate(R.layout.login_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.i("onViewCreated() - mainSharedViewModel.fragmento: %s", mainSharedViewModel.testigo)
        mainSharedViewModel.testigo = LoginFragment::class.java.canonicalName

        /**
         * Como al login se llega desde el destino principal (recycler_view),
         * se debe quitar del back stack para poder salir sin problemas.
         */
        navController.popBackStack(R.id.nav_feed_contents, true)


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        // TODO: Use the ViewModel


    }

    override fun onClick(view: View?) {

        when (view?.id) {

            R.id.link_signup -> {
                hideKeyboard()
                if (viewModel.compruebaConexion(requireContext())) {
                    signUpFirebase(emailUsuario.text.toString(), passUsuario.text.toString())
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.connectivity_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            R.id.btn_login -> {
                hideKeyboard()
                if (viewModel.compruebaConexion(requireContext())) {
                    signInFirebase(emailUsuario.text.toString(), passUsuario.text.toString())
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.connectivity_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun signUpFirebase(email: String, password: String) {

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.d("createUserWithEmail:success")
                    //val navView = requireActivity().toolbar_bottom
                    //navView.visibility = View.VISIBLE
                    /*
                    navGraph.startDestination = R.id.navigation_contents
                    navController.popBackStack(R.id.navigation_login, true)
                     */
                    //navController.navigate(R.id.action_loginFragment_to_feedContentsFragment)
                    navController.popBackStack(R.id.nav_login, true)
                    navController.navigate(R.id.nav_feed_contents)
                    //navController.popBackStack()

                    Toast.makeText(
                        requireContext(), getString(R.string.create_user_with_email_success),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.d(task.exception, "createUserWithEmail:failure")
                    Toast.makeText(
                        requireContext(), "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // ...
            }
    }

    fun signInFirebase(email: String, password: String) {

        //val passUsuario: EditText = input_password

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), getString(R.string.email_input), Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(), getString(R.string.pass_input), Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (password.length < 6) {
            passUsuario.error = getString(R.string.pass_length)
            return
        }

        viewModel.showDialog(requireContext(), getString(R.string.dialog_session))

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->

                if (task.isSuccessful) {
                    Timber.d("task.isSuccessful = true")
                    viewModel.hideDialog()
                    //val navView = requireActivity().toolbar_bottom
                    //navView.visibility = View.VISIBLE
                    /*
                    navGraph.startDestination = R.id.navigation_contents
                    navController.popBackStack(R.id.navigation_login, true)
                     */
                    //navController.navigate(R.id.action_loginFragment_to_feedContentsFragment)
                    navController.popBackStack(R.id.nav_login, true)
                    navController.navigate(R.id.nav_feed_contents)
                    //navController.popBackStack()

                    Toast.makeText(
                        requireContext(), getString(R.string.login_user_with_email_success),
                        Toast.LENGTH_SHORT
                    ).show()
                    // //finish()
                } else {
                    Timber.d("task.isSuccessful = false")
                    viewModel.hideDialog()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.authentication_error), Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

}