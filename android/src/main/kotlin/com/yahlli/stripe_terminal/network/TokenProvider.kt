package com.yahlli.stripe_terminal.network

import android.util.Log
import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider
import com.stripe.stripeterminal.external.models.ConnectionTokenException
import com.yahlli.stripe_terminal.Constants

/**
 * A simple implementation of the [ConnectionTokenProvider] interface. We just request a
 * new token from our backend simulator and forward any exceptions along to the SDK.
 */
class TokenProvider : ConnectionTokenProvider {

    override fun fetchConnectionToken(callback: ConnectionTokenCallback) {
        try {
            Log.d(Constants.TAG, "Calling fetchConnectionToken in tokenProvider: ");
//            Log.d(Constants.TAG, "Calling Api Client : ")
//            Log.d(Constants.TAG, "backendBaseUrl: ${backendBaseUrl}: ");
//            Log.d(Constants.TAG, "requestUrl: ${requestUrl}: ");
////            Log.d(Constants.TAG, "URL: ${MyApiClient.backendBaseUrl + MyApiClient.requestUrl}: ");
//            Log.d(Constants.TAG, "tokenKeyInJson: ${MyApiClient.tokenKeyInJson}: ");
//            Log.d(Constants.TAG, "userAutherizationToken: ${MyApiClient.userAutherizationToken}")
//            AndroidNetworking.post("$backendBaseUrl$requestUrl")
//                .addHeaders("Authorization",MyApiClient.userAutherizationToken)
//                .build()
//                .getAsObject(ConnectionToken::class.java, object : ParsedRequestListener<ConnectionToken> {
//                    override fun onResponse(response: ConnectionToken?) {
//                        Log.d(Constants.TAG, "Calling fetchConnectionToken onSuccess: ${response?.data}");
//                        callback.onSuccess(response!!.data)
//                    }
//
//                    override fun onError(anError: ANError?) {
//                        Log.d(Constants.TAG, "fetchConnectionToken Error : ${anError?.message}");
//                    }
//
//                })
            val token = MyApiClient.createConnectionToken()
            Log.d(Constants.TAG, "Calling fetchConnectionToken onSuccess: ${token}");
            callback.onSuccess(token)
        } catch (e: ConnectionTokenException) {
            Log.d(Constants.TAG, "fetchConnectionToken Error : ${e.message}");
            callback.onFailure(e)
        }
    }
}
