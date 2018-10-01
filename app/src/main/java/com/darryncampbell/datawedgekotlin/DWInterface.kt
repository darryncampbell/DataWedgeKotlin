package com.darryncampbell.datawedgekotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle

class DWInterface()
{
    companion object {

        const val DATAWEDGE_SEND_ACTION = "com.symbol.datawedge.api.ACTION"
        const val DATAWEDGE_RETURN_ACTION = "com.symbol.datawedge.api.RESULT_ACTION"
        const val DATAWEDGE_RETURN_CATEGORY = "android.intent.category.DEFAULT"
        const val DATAWEDGE_EXTRA_SEND_RESULT = "SEND_RESULT"
        const val DATAWEDGE_EXTRA_RESULT = "RESULT"
        const val DATAWEDGE_EXTRA_COMMAND = "COMMAND"
        const val DATAWEDGE_EXTRA_RESULT_INFO = "RESULT_INFO"
        const val DATAWEDGE_EXTRA_RESULT_CODE = "RESULT_CODE"

        const val DATAWEDGE_SCAN_EXTRA_DATA_STRING = "com.symbol.datawedge.data_string"
        const val DATAWEDGE_SCAN_EXTRA_LABEL_TYPE = "com.symbol.datawedge.label_type"

        const val DATAWEDGE_SEND_CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE"

        const val DATAWEDGE_SEND_GET_VERSION = "com.symbol.datawedge.api.GET_VERSION_INFO"
        const val DATAWEDGE_RETURN_VERSION = "com.symbol.datawedge.api.RESULT_GET_VERSION_INFO"
        const val DATAWEDGE_RETURN_VERSION_DATAWEDGE = "DATAWEDGE"

        const val DATAWEDGE_SEND_GET_ENUMERATE_SCANNERS = "com.symbol.datawedge.api.ENUMERATE_SCANNERS"
        const val DATAWEDGE_RETURN_ENUMERATE_SCANNERS = "com.symbol.datawedge.api.RESULT_ENUMERATE_SCANNERS"

        const val DATAWEDGE_SEND_GET_CONFIG = "com.symbol.datawedge.api.GET_CONFIG"
        const val DATAWEDGE_RETURN_GET_CONFIG = "com.symbol.datawedge.api.RESULT_GET_CONFIG"
        const val DATAWEDGE_SEND_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG"

        const val DATAWEDGE_SEND_GET_ACTIVE_PROFILE = "com.symbol.datawedge.api.GET_ACTIVE_PROFILE"
        const val DATAWEDGE_RETURN_GET_ACTIVE_PROFILE = "com.symbol.datawedge.api.RESULT_GET_ACTIVE_PROFILE"

        const val DATAWEDGE_SEND_SWITCH_SCANNER = "com.symbol.datawedge.api.SWITCH_SCANNER"

        const val DATAWEDGE_SEND_SET_SCANNER_INPUT = "com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN"
        const val DATAWEDGE_SEND_SET_SCANNER_INPUT_ENABLE = "ENABLE_PLUGIN"
        const val DATAWEDGE_SEND_SET_SCANNER_INPUT_DISABLE = "DISABLE_PLUGIN"

        const val DATAWEDGE_SEND_SET_SOFT_SCAN = "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER"
    }

    fun sendCommandString(context: Context, command: String, parameter: String, sendResult: Boolean = false)
    {
        val dwIntent = Intent()
        dwIntent.action = DATAWEDGE_SEND_ACTION
        dwIntent.putExtra(command, parameter)
        if (sendResult)
            dwIntent.putExtra(DATAWEDGE_EXTRA_SEND_RESULT, "true")
        context.sendBroadcast(dwIntent)
    }

    fun sendCommandBundle(context: Context, command: String, parameter: Bundle)
    {
        val dwIntent = Intent()
        dwIntent.action = DATAWEDGE_SEND_ACTION
        dwIntent.putExtra(command, parameter)
        context.sendBroadcast(dwIntent)
    }

    fun setConfigForDecoder(context: Context, profileName: String, ean8Value: Boolean,
                            ean13Value: Boolean, code39Value: Boolean, code128Value: Boolean,
                            illuminationValue: String, picklistModeValue: String) {
        val profileConfig = Bundle()
        profileConfig.putString("PROFILE_NAME", profileName)
        profileConfig.putString("PROFILE_ENABLED", "true")
        profileConfig.putString("CONFIG_MODE", "UPDATE")
        val barcodeConfig = Bundle()
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
        barcodeConfig.putString("RESET_CONFIG", "true")
        val barcodeProps = Bundle()
        barcodeProps.putString("scanner_selection", "auto")
        barcodeProps.putString("decoder_ean8", "" + ean8Value)
        barcodeProps.putString("decoder_ean13", "" + ean13Value)
        barcodeProps.putString("decoder_code39", "" + code39Value)
        barcodeProps.putString("decoder_code128", "" + code128Value)
        barcodeProps.putString("illumination_mode", illuminationValue)
        barcodeProps.putString("picklist", picklistModeValue)
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps)
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig)
        sendCommandBundle(context, DATAWEDGE_SEND_SET_CONFIG, profileConfig)
    }
}