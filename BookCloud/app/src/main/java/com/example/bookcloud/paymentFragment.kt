package com.example.bookcloud

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookcloud.DAO.UserDAO
import com.example.bookcloud.model.Libro
import com.example.bookcloud.model.Pedido
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.googlepaylauncher.GooglePayEnvironment
import com.stripe.android.googlepaylauncher.GooglePayLauncher
import com.stripe.android.model.ConfirmPaymentIntentParams
import org.json.JSONObject
import com.example.bookcloud.paymentFragmentArgs

class paymentFragment : Fragment() {

    private lateinit var stripe: Stripe
    private lateinit var googlePayLauncher: GooglePayLauncher
    private lateinit var userDAO: UserDAO
    private lateinit var cardInputWidget: com.stripe.android.view.CardInputWidget
    private lateinit var googlePayButton: Button
    private lateinit var cardPayButton: Button
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var totalPriceText: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var listaPedidos: ArrayList<Pedido>
    private lateinit var librosSeleccionados: List<Libro>
    private var totalPago: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val publishableKey = getString(R.string.tokenStripe)
        PaymentConfiguration.init(requireContext().applicationContext, publishableKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.acitivity_payment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = paymentFragmentArgs.fromBundle(requireArguments())
        librosSeleccionados = args.libros?.toList() ?: emptyList()
        totalPago = librosSeleccionados.sumOf { it.precio?.toDoubleOrNull() ?: 0.0 }

        totalPriceText = view.findViewById(R.id.totalPriceText)
        totalPriceText.text = "Total: $%.2f".format(totalPago)

        stripe = Stripe(requireContext(), PaymentConfiguration.getInstance(requireContext()).publishableKey)
        userDAO = UserDAO(requireContext())
        auth = FirebaseAuth.getInstance()
        listaPedidos = arrayListOf()

        cardInputWidget = view.findViewById(R.id.cardInputWidget)
        googlePayButton = view.findViewById(R.id.googlePayButton)
        cardPayButton = view.findViewById(R.id.cardPayButton)
        nameInput = view.findViewById(R.id.customerName)
        emailInput = view.findViewById(R.id.customerEmail)

        googlePayLauncher = GooglePayLauncher(
            fragment = this,
            config = GooglePayLauncher.Config(
                environment = GooglePayEnvironment.Test,
                merchantCountryCode = "US",
                merchantName = "Tu Empresa"
            ),
            readyCallback = ::onGooglePayReady,
            resultCallback = ::onGooglePayResult
        )

        googlePayButton.setOnClickListener {
            iniciarPagoGooglePay()
        }

        cardPayButton.setOnClickListener {
            iniciarPagoConTarjeta()
        }
    }

    private fun onGooglePayReady(isReady: Boolean) {
        googlePayButton.visibility = if (isReady) View.VISIBLE else View.GONE
    }

    private fun iniciarPagoGooglePay() {
        obtenerClientSecret { clientSecret ->
            clientSecret?.let {
                googlePayLauncher.presentForPaymentIntent(clientSecret)
            } ?: mostrarError("No se pudo obtener el clientSecret")
        }
    }

    private fun iniciarPagoConTarjeta() {
        val params = cardInputWidget.paymentMethodCreateParams
        if (params == null) {
            mostrarError("Completa los datos de tarjeta")
            return
        }

        obtenerClientSecret { clientSecret ->
            if (clientSecret != null) {
                val confirmParams = ConfirmPaymentIntentParams
                    .createWithPaymentMethodCreateParams(params, clientSecret)
                stripe.confirmPayment(requireActivity(), confirmParams)

                // ✅ Siempre vacía el carrito y vuelve al main
                vaciarCarritoYVolver()
            } else {
                mostrarError("No se pudo obtener el clientSecret")
            }
        }
    }

    private fun onGooglePayResult(result: GooglePayLauncher.Result) {
        when (result) {
            is GooglePayLauncher.Result.Completed -> {
                Toast.makeText(requireContext(), "✅ Pago completado con Google Pay", Toast.LENGTH_LONG).show()
                vaciarCarritoYVolver()
            }
            is GooglePayLauncher.Result.Canceled -> {
                Toast.makeText(requireContext(), "Pago cancelado", Toast.LENGTH_SHORT).show()
            }
            is GooglePayLauncher.Result.Failed -> {
                mostrarError("Error: ${result.error.message}")
            }
        }
    }

    private fun vaciarCarritoYVolver() {
        val userId = auth.currentUser?.uid ?: return

        val dbRealtime = FirebaseDatabase
            .getInstance("https://bookcloud-440ad-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("usuarios")
            .child(userId)
            .child("librosCarrito")

        dbRealtime.removeValue().addOnCompleteListener { carritoTask ->
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

            // 1. Eliminar libros de Firestore (colección "books")
            librosSeleccionados.forEach { libro ->
                libro.id?.let { idLibro ->
                    firestore.collection("books").document(idLibro).delete()
                        .addOnSuccessListener {
                            Log.d("Firestore", "Libro $idLibro eliminado de Firestore")
                        }
                        .addOnFailureListener {
                            Log.e("Firestore", "Error eliminando libro $idLibro", it)
                        }
                }
            }

            // 2. Mostrar mensaje y volver al mainFragment
            val mensaje = if (carritoTask.isSuccessful) {
                "Compra realizada"
            } else {
                "Compra realizada (no se pudo vaciar el carrito)"
            }

            Snackbar.make(requireView(), mensaje, Snackbar.LENGTH_LONG).show()

            view?.postDelayed({
                requireActivity()
                    .findNavController(R.id.nav_host_fragment_content_main)
                    .navigate(R.id.action_paymentFragment_to_mainFragment)
            }, 1000)
        }
    }


    private fun obtenerClientSecret(callback: (String?) -> Unit) {
        val url = "https://stripe-backend-s5cg.onrender.com/create-payment-intent"
        val amountInCents = (totalPago * 100).toInt()

        val json = JSONObject().apply {
            put("amount", amountInCents)
            put("currency", "usd")
        }

        val queue = Volley.newRequestQueue(requireContext())
        val request = object : JsonObjectRequest(
            Method.POST, url, json,
            { response ->
                val clientSecret = response.getString("clientSecret")
                callback(clientSecret)
            },
            { error ->
                Log.e("StripeError", "Error: ${error.message}")
                callback(null)
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Content-Type" to "application/json")
            }
        }

        queue.add(request)
    }

    private fun mostrarError(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }
}
