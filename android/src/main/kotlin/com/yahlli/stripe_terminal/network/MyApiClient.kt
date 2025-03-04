package com.yahlli.stripe_terminal.network

import android.util.Log
import com.stripe.stripeterminal.external.models.ConnectionTokenException
import com.yahlli.stripe_terminal.Constants
import com.yahlli.stripe_terminal.model.PaymentIntentCreationResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

/**
 * The `ApiClient` is a singleton object used to make calls to our backend and return their results
 */
object MyApiClient {
    //    const val BACKEND_URL = "10.0.0.2:4242"
    var backendBaseUrl = ""
    var requestUrl = ""
    var tokenKeyInJson = ""
    var userAutherizationToken = ""

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { HttpLoggingInterceptor.Level.BODY })
        .build()
    lateinit var retrofit: Retrofit
    lateinit var service: BackendService

    fun configureApiClient() {
        retrofit = Retrofit.Builder()
            .baseUrl("$backendBaseUrl$requestUrl/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(BackendService::class.java)

    }


    @Throws(ConnectionTokenException::class)
    internal fun createConnectionToken(): String {
        try {
            Log.d(Constants.TAG, "Calling Api Client : ")
//            Log.d(Constants.TAG, "backendBaseUrl: ${backendBaseUrl}: ");
//            Log.d(Constants.TAG, "requestUrl: ${requestUrl}: ");
            Log.d(Constants.TAG, "URL: ${backendBaseUrl + requestUrl}: ");
            Log.d(Constants.TAG, "tokenKeyInJson: ${tokenKeyInJson}: ");
            Log.d(Constants.TAG, "userAutherizationToken: ${userAutherizationToken}")
            val result = service.getConnectionToken(
                userAutherizationToken
            ).execute()
            if (result.isSuccessful && result.body() != null) {
                Log.d(Constants.TAG, "Result is successful: ${result.code()}");
                return result.body()!!.data!!
            } else {
                Log.d(Constants.TAG, "Result is not successful: ${result.code()}");
                throw ConnectionTokenException("Creating connection token failed: ${result.code()}")
            }
        } catch (e: IOException) {
            Log.d(Constants.TAG, "Result failed exception: ${e.message}");
            throw ConnectionTokenException("Creating connection token failed", e)
        }
    }

    internal fun createLocation(
        displayName: String?,
        city: String?,
        country: String?,
        line1: String?,
        line2: String?,
        postalCode: String?,
        state: String?,
    ) {
        TODO("Call Backend application to create location")
    }

    internal fun capturePaymentIntent(id: String) {
        service.capturePaymentIntent(id).execute()
    }

    /**
     * This method is calling the example backend (https://github.com/stripe/example-terminal-backend)
     * to create paymentIntent for Internet based readers, for example WisePOS E. For your own application, you
     * should create paymentIntent in your own merchant backend.
     */
    internal fun createPaymentIntent(
        amount: Long,
        currency: String,
        callback: Callback<PaymentIntentCreationResponse>
    ) {
        service.createPaymentIntent(amount, currency).enqueue(callback)
    }
}
