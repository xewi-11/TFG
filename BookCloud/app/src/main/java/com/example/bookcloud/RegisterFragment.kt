package com.example.bookcloud

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.bookcloud.databinding.FragmentRegisterBinding
import com.example.bookcloud.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // This property is only valid between onCreateView and
    // onDestroyView.


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRegisterBinding.inflate(layoutInflater)
        auth=FirebaseAuth.getInstance()
        database=FirebaseDatabase.getInstance("https://comprasbd-ed8ae-default-rtdb.europe-west1.firebasedatabase.app/")
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

      binding.bntLoginRegister.setOnClickListener {
          if (binding.editCorreo.text.isNotEmpty() && binding.editPass.text.isNotEmpty() && binding.editPass2.text.isNotEmpty() && binding.editNombre.text.isNotEmpty()&& binding.editApellido.text.isNotEmpty() && binding.editPass.text.equals(binding.editPass2.text)) {

              auth.createUserWithEmailAndPassword(binding.editCorreo.text.toString(), binding.editPass.text.toString())
                  .addOnCompleteListener {
                      if (it.isSuccessful) {
                          val usuario = Usuario(auth.currentUser!!.uid,
                              binding.editNombre.text.toString(),
                                binding.editApellido.text.toString(),
                              binding.editCorreo.text.toString(),
                              binding.editPass.text.toString()
                          )
                          val reference = database.reference.child("usuarios").child(auth.currentUser!!.uid)
                          reference.setValue(usuario).addOnCompleteListener {
                              if(it.isSuccessful){
                                 // o ir al main o a un cuadro de dialogo que diga que se ha registrado correctamente y si quiere loguearse
                                  findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                              }
                          }
                      }
                  }
          }
      }
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}