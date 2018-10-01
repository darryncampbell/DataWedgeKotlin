package com.darryncampbell.datawedgekotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DWReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        //  This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        //  Notify registered observers
        ObservableObject.instance.updateValue(intent)
    }
}
