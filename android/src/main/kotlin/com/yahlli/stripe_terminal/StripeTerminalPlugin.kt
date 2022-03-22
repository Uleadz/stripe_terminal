package com.yahlli.stripe_terminal

import androidx.annotation.NonNull
import com.stripe.stripeterminal.Terminal

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
/** StripeTerminalPlugin */
class StripeTerminalPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "stripe_terminal")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        } else if (call.method == "setupConnectionTokenProvider") {
            val backendBaseUrl = call.argument<String>("backendBaseUrl")
            val requestUrl = call.argument<String>("requestUrl")
            val tokenKeyInJson = call.argument<String>("tokenKeyInJson")
            val userAutherizationToken = call.argument<String>("userAutherizationToken")

            setupConnectionTokenProvider(
                backendBaseUrl!!,
                requestUrl!!,
                tokenKeyInJson!!,
                userAutherizationToken!!,
                result
            )
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    fun setupConnectionTokenProvider(
        backendBaseUrl:String,
        requestUrl:String,
        tokenKeyInJson:String,
        userAutherizationToken:String,
        @NonNull result: Result
    ) {
//        MyApiClient.backendBaseUrl = backendBaseUrl
//        MyApiClient.requestUrl = requestUrl
//        MyApiClient.tokenKeyInJson = tokenKeyInJson
//        MyApiClient.userAutherizationToken = userAutherizationToken
//

    }


}
