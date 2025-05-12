package com.example.bookcloud

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.googlepaylauncher.GooglePayEnvironment
import com.stripe.android.googlepaylauncher.GooglePayLauncher
import com.stripe.android.model.ConfirmPaymentIntentParams
import org.json.JSONObject

class paymentFragment: Fragment() {
    private lateinit var stripe: Stripe
    private lateinit var googlePayLauncher: GooglePayLauncher

    private lateinit var cardInputWidget: com.stripe.android.view.CardInputWidget
    private lateinit var googlePayButton: Button
    private lateinit var cardPayButton: Button
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.acitivity_payment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init Stripe
        PaymentConfiguration.init(requireContext(), R.string.tokenStripe.toString())
        stripe = Stripe(requireContext(), PaymentConfiguration.getInstance(requireContext()).publishableKey)

        // UI refs
        cardInputWidget = view.findViewById(R.id.cardInputWidget)
        googlePayButton = view.findViewById(R.id.googlePayButton)
        cardPayButton = view.findViewById(R.id.cardPayButton)
        nameInput = view.findViewById(R.id.customerName)
        emailInput = view.findViewById(R.id.customerEmail)

        // Google Pay Setup
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
            } else {
                mostrarError("No se pudo obtener el clientSecret")
            }
        }
    }

    private fun onGooglePayResult(result: GooglePayLauncher.Result) {
        when (result) {
            is GooglePayLauncher.Result.Completed -> {
                Toast.makeText(requireContext(), "✅ Pago completado con Google Pay", Toast.LENGTH_LONG).show()
            }
            is GooglePayLauncher.Result.Canceled -> {
                Toast.makeText(requireContext(), "Pago cancelado", Toast.LENGTH_SHORT).show()
            }
            is GooglePayLauncher.Result.Failed -> {
                mostrarError("Error: ${result.error.message}")
            }
        }
    }

    private fun obtenerClientSecret(callback: (String?) -> Unit) {
        val url =
            "https://stripe-backend-s5cg.onrender.com/create-payment-intent"

        val json = JSONObject()
        json.put("amount", 5000)
        json.put("currency", "usd")

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