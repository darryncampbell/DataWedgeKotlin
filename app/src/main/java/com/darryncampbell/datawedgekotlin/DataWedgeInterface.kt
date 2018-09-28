package com.darryncampbell.datawedgekotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle

class DataWedgeInterface()
{
    companion object {
        val DATAWEDGE_SEND_GET_VERSION = "com.symbol.datawedge.api.GET_VERSION_INFO"
        val DATAWEDGE_RETURN_VERSION = "com.symbol.datawedge.api.RESULT_GET_VERSION_INFO"
        val DATAWEDGE_RETURN_VERSION_DATAWEDGE = "DATAWEDGE"
        val DATAWEDGE_RETURN_ACTION = "com.symbol.datawedge.api.RESULT_ACTION"
        val DATAWEDGE_RETURN_CATEGORY = "android.intent.category.DEFAULT"
    }
    init {

    }

    fun sendCommandString(context: Context, command: String, parameter: String)
    {
        val dwIntent = Intent();
        dwIntent.setAction("com.symbol.datawedge.api.ACTION")
        dwIntent.putExtra(command, parameter)
        context.sendBroadcast(dwIntent)
    }

    fun sendCommandBundle(command: Bundle)
    {

    }
}