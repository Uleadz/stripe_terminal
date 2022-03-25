package com.yahlli.stripe_terminal

import android.util.Log
import com.stripe.stripeterminal.external.callable.TerminalListener
import com.stripe.stripeterminal.external.models.ConnectionStatus
import com.stripe.stripeterminal.external.models.PaymentStatus
import com.stripe.stripeterminal.external.models.Reader

/**
 * The `TerminalEventListener` implements the [TerminalListener] interface and will
 * forward along any events to other parts of the app that register for updates.
 *
 * TODO: Finish implementing
 */
class TerminalEventListener : TerminalListener {

    override fun onUnexpectedReaderDisconnect(reader: Reader) {
        Log.d(Constants.TAG, "TerminalEventListener onUnexpectedReaderDisconnect called: ");
        Log.i("UnexpectedDisconnect", reader.serialNumber ?: "reader's serialNumber is null!")
    }

    override fun onConnectionStatusChange(status: ConnectionStatus) {
        Log.d(Constants.TAG, "TerminalEventListener onConnectionStatusChange called: ")
        Log.i("ConnectionStatusChange", status.toString())
    }

    override fun onPaymentStatusChange(status: PaymentStatus) {
        Log.d(Constants.TAG, "TerminalEventListener onPaymentStatusChange called: ")
        Log.i("PaymentStatusChange", status.toString())
    }
}
