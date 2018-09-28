package com.darryncampbell.datawedgekotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DataWedgeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val i = 1;
        throw UnsupportedOperationException("Not yet implemented")
    }
}
