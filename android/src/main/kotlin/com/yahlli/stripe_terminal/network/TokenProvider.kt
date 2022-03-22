//package com.yahlli.stripe_terminal.model
//
//import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback
//import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider
//import com.stripe.stripeterminal.external.models.ConnectionTokenException
//import com.yahlli.stripe_terminal.network.MyApiClient
//
///**
// * A simple implementation of the [ConnectionTokenProvider] interface. We just request a
// * new token from our backend simulator and forward any exceptions along to the SDK.
// */
//class TokenProvider : ConnectionTokenProvider {
//
//    override fun fetchConnectionToken(callback: ConnectionTokenCallback) {
//        try {
////            Log.d(Constants.TAG, "Calling fetchConnectionToken in tokenProvider: ");
//            val token = MyApiClient.createConnectionToken()
//            callback.onSuccess(token)
//        } catch (e: ConnectionTokenException) {
////            Log.d(Constants.TAG, "fetchConnectionToken Error : ${e.message}");
//            callback.onFailure(e)
//        }
//    }
//}
