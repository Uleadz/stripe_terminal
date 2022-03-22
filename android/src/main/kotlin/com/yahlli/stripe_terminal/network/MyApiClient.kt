//package com.yahlli.stripe_terminal.network
//
//import com.yahlli.stripe_terminal.model.PaymentIntentCreationResponse
//import com.stripe.stripeterminal.external.models.ConnectionTokenException
//import okhttp3.OkHttpClient
//import retrofit2.Callback
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import java.io.IOException
//
///**
// * The `ApiClient` is a singleton object used to make calls to our backend and return their results
// */
//object MyApiClient {
//    //    const val BACKEND_URL = "10.0.0.2:4242"
//    var backendBaseUrl = ""
//    var requestUrl = ""
//    var tokenKeyInJson = ""
//    var userAutherizationToken = ""
//
//    private val client = OkHttpClient.Builder()
//        .build()
//    lateinit var retrofit: Retrofit;
//    lateinit var service: BackendService
//
//    public fun configureApiClient() {
//        retrofit = Retrofit.Builder()
//        .baseUrl(backendBaseUrl)
//            .client(client)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        service = retrofit.create(BackendService::class.java)
//
//    }
//
//
//    @Throws(ConnectionTokenException::class)
//    internal fun createConnectionToken(): String {
//        try {
////            Log.d(Constants.TAG, "Calling Api Client : ")
//            val result = service.getConnectionToken().execute()
//            if (result.isSuccessful && result.body() != null) {
//                return result.body()!!.secret
//            } else {
//                throw ConnectionTokenException("Creating connection token failed: ${result.code()}")
//            }
//        } catch (e: IOException) {
//            throw ConnectionTokenException("Creating connection token failed", e)
//        }
//    }
//
//    internal fun createLocation(
//        displayName: String?,
//        city: String?,
//        country: String?,
//        line1: String?,
//        line2: String?,
//        postalCode: String?,
//        state: String?,
//    ) {
//        TODO("Call Backend application to create location")
//    }
//
//    internal fun capturePaymentIntent(id: String) {
//        service.capturePaymentIntent(id).execute()
//    }
//
//    /**
//     * This method is calling the example backend (https://github.com/stripe/example-terminal-backend)
//     * to create paymentIntent for Internet based readers, for example WisePOS E. For your own application, you
//     * should create paymentIntent in your own merchant backend.
//     */
//    internal fun createPaymentIntent(
//        amount: Long,
//        currency: String,
//        callback: Callback<PaymentIntentCreationResponse>
//    ) {
//        service.createPaymentIntent(amount, currency).enqueue(callback)
//    }
//}
