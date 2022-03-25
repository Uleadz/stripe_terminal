package com.yahlli.stripe_terminal

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.gsonparserfactory.GsonParserFactory
import com.google.gson.GsonBuilder
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.external.callable.*
import com.stripe.stripeterminal.external.models.*
import com.stripe.stripeterminal.log.LogLevel
import com.yahlli.stripe_terminal.network.TokenProvider
import com.yahlli.stripe_terminal.network.MyApiClient
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** StripeTerminalPlugin */
class StripeTerminalPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private lateinit var activity: Activity
    private var taskCancelable: Cancelable? = null
    private var paymentIntent: PaymentIntent? = null
    val availableReaders = mutableListOf<Reader>()
    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        Log.d(Constants.TAG, "onAttachedEngine called drazz: ")
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "stripe_terminal")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext



        AndroidNetworking.initialize(context)
        AndroidNetworking.enableLogging(com.androidnetworking.interceptors.HttpLoggingInterceptor.Level.BODY)
        AndroidNetworking.setParserFactory(GsonParserFactory(GsonBuilder().setLenient().create()))
    }

    override fun onDetachedFromActivity() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onMethodCall(
        call: MethodCall,
        result: io.flutter.plugin.common.MethodChannel.Result
    ) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "setupConnectionTokenProvider" -> {
                Log.d(Constants.TAG, "setupConnectionTokenProvider called: ")
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
            }
            "discoverReaders" -> {
                ensureTerminalInitialized(result) {
                    discoverReaders(
                        call.argument<Boolean>("simulated")!!,
                        result
                    )

                }
            }
            "connectBluetoothReader" -> {
                ensureTerminalInitialized(result) {
                    connectBluetoothReader(
                        call.argument<String>("selectedReaderSerialNumber")!!,
                        call.argument<String>("locationId")!!,
                        call.argument<String>("simulateReaderUpdate")!!,
                        result
                    )

                }
            }
            "charge" -> {
                ensureTerminalInitialized(result) {
                    charge(
                        call.argument<String>("paymentIntent")!!,
                        result
                    )

                }
            }
            "disconnectBluetoothReader" -> {
                ensureTerminalInitialized(result) {
                    disconnectedBluetoothReader(result)

                }
            }
            "cancelCurrentTask" -> {
                ensureTerminalInitialized(result) {
                    cancelCurrentTask(result)

                }
            }
            "clearCachedCredentials" -> {
                ensureTerminalInitialized(result) {
                    clearCachedCredentials(result)

                }
            }
            "cancelPaymentIntent" -> {
                ensureTerminalInitialized(result) {
                    cancelPaymentIntent(result)
                }
            }
            "updateReader" -> {
                ensureTerminalInitialized(result){
                    updateReader()
                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun updateReader() {

        val connectedReader = Terminal.getInstance()
            .connectedReader
        connectedReader?.let {
            it.availableUpdate?.let { it1 -> it.update(it1) }
        }
    }

    private fun onTerminalNotInitializedError(result: Result) {
        result.error(
            "Terminal not initialized",
            "Please initialize terminal and then try to get it's instance",
            "error"
        )
    }

    private fun ensureTerminalInitialized(result: Result, onSuccess: () -> Unit) {
        when {
            Terminal.isInitialized() -> {
                onSuccess()
            }
            else -> {
                onTerminalNotInitializedError(result)
            }
        }
    }

    private fun cancelPaymentIntent(result: Result) {
        if (paymentIntent == null) {
            Log.d(Constants.TAG, "No payment intent to cancel")
            return
        }
        try {
            Terminal.getInstance()
                .cancelPaymentIntent(paymentIntent!!, object : PaymentIntentCallback {
                    override fun onFailure(e: TerminalException) {
                        Log.d(Constants.TAG, "Canceling payment intent failed:d: ${e.message}");
                        result.error(
                            "cancelPaymentIntent",
                            e.localizedMessage,
                            "error"
                        )
                    }

                    override fun onSuccess(paymentIntent: PaymentIntent) {
                        Log.d(Constants.TAG, "cancelPaymentIntent succeeded")
                        result.success(null)
                    }

                })
        } catch (e: Exception) {
            Log.d(Constants.TAG, "Canceling payment intent failed:d: ${e.message}");
            result.error(
                "cancelPaymentIntent",
                e.localizedMessage,
                "error"
            )
        }
    }

    private fun clearCachedCredentials(result: Result) {
        Terminal.getInstance().clearCachedCredentials()
        Log.d(Constants.TAG, "clearCachedCredentials success");
    }

    private fun cancelCurrentTask(result: Result) {
        if (taskCancelable == null) {
            Log.d(Constants.TAG, "Nothing to cancel")
            result.success(null)
        } else {
            taskCancelable!!.cancel(object : Callback {
                override fun onFailure(e: TerminalException) {
                    Log.d(Constants.TAG, "taskCancelable failed: ${e.message}");
                    result.error(
                        "taskCancelable",
                        e.localizedMessage,
                        "error"
                    )
                }

                override fun onSuccess() {
                    Log.d(Constants.TAG, "Successfully canceled task")
                    result.success(null)
                }

            })
        }
    }

    private fun charge(paymentIntent: String, result: Result) {


        try {
            Log.d(Constants.TAG, "Charging payment intent: ${paymentIntent} ");
            val processPaymentCallback by lazy {
                object : PaymentIntentCallback {
                    override fun onSuccess(paymentIntent: PaymentIntent) {
                        Log.d(Constants.TAG, "processPayment succeeded");
                        result.success(paymentIntent.id)
                    }

                    override fun onFailure(e: TerminalException) {
                        Log.d(Constants.TAG, "processPaymentCallback failed: ${e.message}");
                        result.error(
                            "processPaymentCallbackFailed",
                            e.localizedMessage,
                            "error"
                        )
                    }
                }
            }


            val collectPaymentMethodCallback by lazy {
                object : PaymentIntentCallback {
                    override fun onSuccess(paymentIntent: PaymentIntent) {
                        Log.d(Constants.TAG, "collectPaymentMethod succeeded");
                        Terminal.getInstance().processPayment(paymentIntent, processPaymentCallback)
                    }

                    override fun onFailure(e: TerminalException) {
                        Log.d(Constants.TAG, "collectPaymentMethodCallback failed: ${e.message}");
                        result.error(
                            "collectPaymentMethodCallbackFailed",
                            e.localizedMessage,
                            "error"
                        )
                    }
                }
            }

            val createPaymentIntentCallback by lazy {
                object : PaymentIntentCallback {
                    override fun onSuccess(paymentIntent: PaymentIntent) {
                        Log.d(Constants.TAG, "createPaymentIntent succeeded");
                        this@StripeTerminalPlugin.paymentIntent = paymentIntent
//                        Terminal.getInstance().simulatorConfiguration = SimulatorConfiguration(
//                            simulatedCard = SimulatedCard(
//                                SimulatedCardType.CHARGE_DECLINED_INSUFFICIENT_FUNDS
//                            )
//                        )
                        taskCancelable = Terminal.getInstance()
                            .collectPaymentMethod(paymentIntent, collectPaymentMethodCallback)
                    }

                    override fun onFailure(e: TerminalException) {
                        Log.d(Constants.TAG, "createPaymentIntent failed: ${e.message}");
                        result.error(
                            "createPaymentIntentFailed",
                            e.localizedMessage,
                            "error"
                        )
                    }
                }
            }

            Terminal.getInstance().retrievePaymentIntent(
                paymentIntent,
                createPaymentIntentCallback
            )
        } catch (e: Exception) {
            result.error(
                "chargeFailed",
                e.localizedMessage,
                "error"
            )
        }
    }

    private fun disconnectedBluetoothReader(result: Result) {
        Terminal.getInstance().disconnectReader(object : Callback {
            override fun onFailure(e: TerminalException) {
                Log.d(Constants.TAG, "disconnectReader failed: ${e.message}");
                result.error(
                    "disconnectBluetoothReaderFailed",
                    e.localizedMessage,
                    "error"
                )
            }

            override fun onSuccess() {
                Log.d(Constants.TAG, "Successfully disconnected from reader")
                result.success(null)
            }

        })
    }

    private fun connectBluetoothReader(
        selectedReaderSerialNumber: String,
        locationId: String,
        simulateReaderUpdate: String,
        result: Result
    ) {
        val selectedReader =
            availableReaders.firstOrNull { it.serialNumber == selectedReaderSerialNumber }
        if (selectedReader == null) {
            print("Unknown reader selected")
            result.error(
                "connectBluetoothReaderFailed",
                "Unknown reader selected",
                "Unknown reader selected"
            )
            return
        }

        val connectionConfig = ConnectionConfiguration.BluetoothConnectionConfiguration(
            locationId
        )

        val simulateReaderUpdateValue = SimulateReaderUpdate.valueOf(simulateReaderUpdate)

        Terminal.getInstance().simulatorConfiguration = SimulatorConfiguration(
            update = simulateReaderUpdateValue
        )

        val readerCallback = object : ReaderCallback {

            override fun onSuccess(reader: Reader) {
                Log.d(Constants.TAG, "Successfully connected to reader: ${reader}");
                result.success(null)
            }

            override fun onFailure(e: TerminalException) {
                Log.d(Constants.TAG, "connectReader failed ${selectedReader}");
                result.error(
                    "connectBluetoothReaderFailed",
                    e.localizedMessage, "error"
                )
            }
        }

        Terminal.getInstance().connectBluetoothReader(
            selectedReader,
            connectionConfig,
            object : BluetoothReaderListener {

                override fun onFinishInstallingUpdate(
                    update: ReaderSoftwareUpdate?,
                    e: TerminalException?
                ) {
                    Log.d(Constants.TAG, "onFinishInstallingUpdate: ${update}")
                    Handler(Looper.getMainLooper()).post{
                        channel.invokeMethod(
                            "onFinishInstallingUpdate",
                            true
                        )

                    }
                    super.onFinishInstallingUpdate(update, e)
                }

                override fun onReportAvailableUpdate(update: ReaderSoftwareUpdate) {
                    Log.d(Constants.TAG, "onReportAvailableUpdate: called ");
                    super.onReportAvailableUpdate(update)
                }

                override fun onReportReaderSoftwareUpdateProgress(progress: Float) {
                    Log.d(Constants.TAG, "onReportReaderSoftwareUpdateProgress: ${progress} ")
                    Handler(Looper.getMainLooper()).post{
                        channel.invokeMethod(
                            "updateProgress",
                            progress
                        )

                    }
                    super.onReportReaderSoftwareUpdateProgress(progress)
                }

                override fun onRequestReaderDisplayMessage(message: ReaderDisplayMessage) {
                    Log.d(Constants.TAG, "onRequestReaderDisplayMessage: ${message}");
                    super.onRequestReaderDisplayMessage(message)
                }

                override fun onStartInstallingUpdate(
                    update: ReaderSoftwareUpdate,
                    cancelable: Cancelable?
                ) {
                    Log.d(Constants.TAG, "onStartInstallingUpdate called: ");
                    Handler(Looper.getMainLooper()).post{
                        channel.invokeMethod(
                            "isUpdateRequired",
                            true
                        )
                    }
                    super.onStartInstallingUpdate(update, cancelable)
                }
            },
            readerCallback
        )
    }

    private fun discoverReaders(simulated: Boolean, result: Result) {

        Log.d(Constants.TAG, "discoverReaders called");
        val discoveryConfig =
            DiscoveryConfiguration(0, DiscoveryMethod.BLUETOOTH_SCAN, simulated)


        try {
            val discoveryCallback = object : Callback {
                override fun onSuccess() {
                    result.success(null)
                }

                override fun onFailure(e: TerminalException) {
                    result.error(
                        "discoverReadersFailed",
                        e.localizedMessage,
                        "error"
                    )

                }
            }

            val discoveryListener = object : DiscoveryListener {
                override fun onUpdateDiscoveredReaders(readers: List<Reader>) {
                    availableReaders.clear()
                    availableReaders.addAll(readers)
                    Log.d(Constants.TAG, "Got available readers: ${availableReaders}");
//                    channel.invokeMethod("didUpdateDiscoveredReaders", readers)
                    Handler(Looper.getMainLooper()).post {
                        val readersJson = mutableListOf<Map<String, Any?>>()
                        readers.forEach {
                            readersJson.add(
                                mapOf<String, Any?>(
                                    "serialNumber" to it.serialNumber.toString(),
                                    "batteryLevel" to it.batteryLevel,
                                    "locationId" to it.location?.id.toString(),
                                )
                            )

                        }
                        channel.invokeMethod(
                            "didUpdateDiscoveredReaders",
                            readersJson
                        )
                        // Call the desired channel message here.
                    }

                }
            }

            taskCancelable = Terminal.getInstance()
                .discoverReaders(discoveryConfig, discoveryListener, discoveryCallback)


        } catch (e: Exception) {
            result.error(
                "discoverReadersFailed",
                e.localizedMessage,
                "error"
            )

        }


    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    fun setupConnectionTokenProvider(
        backendBaseUrl: String,
        requestUrl: String,
        tokenKeyInJson: String,
        userAutherizationToken: String,
        result: Result
    ) {

        Log.d(Constants.TAG, "setupConnectionTokenProvider inside called: ")
        MyApiClient.backendBaseUrl = backendBaseUrl
        MyApiClient.requestUrl = requestUrl
        MyApiClient.tokenKeyInJson = tokenKeyInJson
        MyApiClient.userAutherizationToken = "Bearer ${userAutherizationToken}"
//        MyApiClient.userAutherizationToken = userAutherizationToken

        MyApiClient.configureApiClient()

        if (Terminal.isInitialized()) {
            Log.d(Constants.TAG, "Terminal already have connection token connected")
            result.success(null)
            print("Terminal already have connection token connected")
            return
        }

        Log.d(Constants.TAG, "Initialising Terminal")


        try {
            Terminal.initTerminal(
                context, LogLevel.VERBOSE,
                TokenProvider(),
                TerminalEventListener()
            )

            result.success(null)
        } catch (e: Exception) {
            Log.d(Constants.TAG, "Terminal initialization failed: ${e.message}")
            result.error(
                "Terminal initialization failed",
                e.localizedMessage,
                "error"
            )
        }
    }


}
