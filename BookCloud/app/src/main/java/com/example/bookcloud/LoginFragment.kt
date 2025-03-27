package com.example.bookcloud

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.bookcloud.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLoginBinding.inflate(layoutInflater)
        auth=FirebaseAuth.getInstance()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

      binding.bntLogin.setOnClickListener {
          if (binding.editCorreo.text.isNotEmpty() && binding.editPass.text.isNotEmpty()){
              auth.signInWithEmailAndPassword(binding.editCorreo.text.toString(),binding.editPass.text.toString()).addOnCompleteListener {
                  if (it.isSuccessful){
                      //cambiar a main
                  }else{
                      Snackbar.make(view,"Error,no hay usuario con ese correo o contraseña",Snackbar.LENGTH_LONG).show()
                  }
              }
          }
      }
        binding.textRegistar.setOnClickListener{
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}