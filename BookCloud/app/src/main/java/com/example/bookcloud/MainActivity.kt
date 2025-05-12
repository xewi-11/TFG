package com.example.bookcloud

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.example.bookcloud.Ui.dialog.DialogProfileUser
import com.example.bookcloud.adapter.AdapterBook
import com.example.bookcloud.databinding.ActivityMainBinding
import com.example.bookcloud.model.Libro
import com.google.android.material.snackbar.Snackbar
import com.stripe.android.PaymentConfiguration

class MainActivity: AppCompatActivity(),AdapterBook.OnBookListener,DialogProfileUser.onRequestConfirmacion {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.fab.setOnClickListener { view ->

        }

        // Init Stripe
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp()
                || super.onSupportNavigateUp()
    }
    override fun onBookClick(libro: Libro) {
        Snackbar.make(binding.root, "Libro seleccionado: ${libro.nombre}", Snackbar.LENGTH_SHORT).show()
    }

    override fun onConfirmacion(respuesta: Boolean) {
        if(respuesta){
            Snackbar.make(binding.root,"Usuario Seleccionado correctamente",Snackbar.LENGTH_LONG).show()
        }else{
            Snackbar.make(binding.root,"Error al seleccionar el usuario",Snackbar.LENGTH_LONG).show()
        }
    }
}
