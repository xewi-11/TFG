package com.example.bookcloud.Ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.bookcloud.R
import com.example.bookcloud.databinding.ViewRecycledBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DialogProfileUser:DialogFragment(){

    private lateinit var auth: FirebaseAuth
    private lateinit var textNombre:EditText
    private lateinit var textApellido:EditText
    private lateinit var textCorreo:EditText
    private lateinit var textPassword:EditText
    private lateinit var vista: View
    private lateinit var dialog:DialogProfileUser
    private lateinit var database: FirebaseDatabase


    override fun onAttach(context: Context) {
        super.onAttach(context)
        dialog= DialogProfileUser()
        auth= FirebaseAuth.getInstance()
        database=FirebaseDatabase.getInstance("https://bookcloud-440ad-default-rtdb.europe-west1.firebasedatabase.app/")
        vista=LayoutInflater.from(context).inflate(R.layout.view_detail_user,null)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder:AlertDialog.Builder=AlertDialog.Builder(requireActivity())
        builder.setView(vista)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        textNombre=vista.findViewById(R.id.textNombre)
        textApellido=vista.findViewById(R.id.textApellido)
        textCorreo=vista.findViewById(R.id.textCorreo)
        textPassword=vista.findViewById(R.id.editTextPassword)

    }
    fun cogerDatos(){

    }

}