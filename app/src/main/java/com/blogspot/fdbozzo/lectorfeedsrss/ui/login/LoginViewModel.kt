package com.blogspot.fdbozzo.lectorfeedsrss.ui.login

import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class LoginViewModel : ViewModel() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var alertDialog: AlertDialog
    private lateinit var progressDialog: ProgressDialog

    init {
        //mAuth = Firebase.auth
    }

    //COMPROBAR CONEXION INTERNET.
    fun compruebaConexion(context: Context): Boolean {

        var connected = false

        val connec = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Recupera todas las redes (tanto móviles como wifi)
        val redes = connec.allNetworkInfo

        for (i in redes.indices) {
            // Si alguna red tiene conexión, se devuelve true
            if (redes[i].state == NetworkInfo.State.CONNECTED) {
                connected = true
            }
        }
        return connected
    }

    // Hacer actividades transprentes.
    fun activityTransparent(activity: AppCompatActivity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    //Dialogo perso.
    fun showDialog(contexto: Context, mensaje: String) {
        //alertDialog = SpotsDialog(contexto, mensage)
        //alertDialog.show()
        val builder = AlertDialog.Builder(contexto)
        builder.setMessage(mensaje)
        builder.setCancelable(false)
        alertDialog = builder.create()
        alertDialog.show()
    }

    // Cerrar dialogo.
    fun hideDialog() {
        alertDialog.dismiss()
    }

    // DIALOGO NORMAL.
    fun showProgressDialog(title: String, message: String, contexto: Context) {
        if (progressDialog.isShowing)
            progressDialog.setMessage(message)
        else
            progressDialog = ProgressDialog.show(contexto, title, message, true, false)
    }

    // CERRAR DIALOGO.
    fun hideProgressDialog() {
        progressDialog.hide()
    }

    // Cerrar sesiones.
    /*
    fun signOut(auth: FirebaseAuth, client: GoogleSignInClient) {
        auth.signOut()
        client.signOut()
    }
    */

}