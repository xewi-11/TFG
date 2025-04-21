package com.example.bookcloud

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.stripe.android.PaymentConfiguration
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.googlepaylauncher.GooglePayEnvironment
import com.stripe.android.googlepaylauncher.GooglePayLauncher
import com.stripe.android.model.*
import org.json.JSONObject

class PaymentActivity : AppCompatActivity() {

    private lateinit var stripe: Stripe
    private lateinit var googlePayLauncher: GooglePayLauncher
    private lateinit var cardInputWidget: com.stripe.android.view.CardInputWidget
    private lateinit var googlePayButton: Button
    private lateinit var cardPayButton: Button
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivity_payment)

        // Stripe Init
        PaymentConfiguration.init(applicationContext, "pk_test_TU_CLAVE_PUBLICA")
        stripe = Stripe(applicationContext, PaymentConfiguration.getInstance(applicationContext).publishableKey)

        // UI
        cardInputWidget = findViewById(R.id.cardInputWidget)
        googlePayButton = findViewById(R.id.googlePayButton)
        cardPayButton = findViewById(R.id.cardPayButton)
        nameInput = findViewById(R.id.customerName)
        emailInput = findViewById(R.id.customerEmail)

        // Google Pay Setup
        googlePayLauncher = GooglePayLauncher(
            activity = this,
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
                stripe.confirmPayment(this, confirmParams)
            } else {
                mostrarError("No se pudo obtener el clientSecret")
            }
        }
    }

    private fun onGooglePayResult(result: GooglePayLauncher.Result) {
        when (result) {
            is GooglePayLauncher.Result.Completed -> {
                Toast.makeText(this, "✅ Pago completado con Google Pay", Toast.LENGTH_LONG).show()
            }
            is GooglePayLauncher.Result.Canceled -> {
                Toast.makeText(this, "Pago cancelado", Toast.LENGTH_SHORT).show()
            }
            is GooglePayLauncher.Result.Failed -> {
                mostrarError("Error: ${result.error.message}")
            }
        }
    }

    private fun obtenerClientSecret(callback: (String?) -> Unit) {
        val url = "https://stripe-backend-abc123.onrender.com/create-payment-intent" // cambia esto a tu URL real

        val json = JSONObject()
        json.put("amount", 5000) // $50.00 USD
        json.put("currency", "usd")

        val queue = Volley.newRequestQueue(this)
        val request = object : JsonObjectRequest(Method.POST, url, json,
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
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        stripe.onPaymentResult(requestCode, data, object : com.stripe.android.ApiResultCallback<PaymentIntentResult> {
            override fun onSuccess(result: PaymentIntentResult) {
                val status = result.intent.status
                if (status == StripeIntent.Status.Succeeded) {
                    Toast.makeText(this@PaymentActivity, "✅ Pago exitoso", Toast.LENGTH_LONG).show()
                } else {
                    mostrarError("❌ Pago no completado")
                }
            }

            override fun onError(e: Exception) {
                mostrarError("❌ Error: ${e.localizedMessage}")
            }
        })
    }
}