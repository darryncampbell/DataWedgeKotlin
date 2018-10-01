package com.darryncampbell.datawedgekotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_configuration.*
import java.util.*

class ConfigurationActivity : AppCompatActivity(), Observer, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private val dwInterface = DWInterface();
    private var activeProfile = "";
    private var ean8 = false
    private var ean13 = false
    private var code39 = false
    private var code128 = false
    private var illuminationMode = false
    private var illuminationModeValue = "off"
    private var picklistMode = false
    private var picklistModeValue = "0"
    private var selected_scanner_index = 0

    companion object {
        val SETTINGS_KEY_VERSION = "version"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ObservableObject.instance.addObserver(this)
        val version65OrOver = getIntent().getBooleanExtra(SETTINGS_KEY_VERSION, false)
        if (version65OrOver)
        {
            //  Only enable the ability to control the scanner if the DataWedge version is 6.5 or
            //  greater
            enableControls()
            dwInterface.sendCommandString(this, DWInterface.DATAWEDGE_SEND_GET_ACTIVE_PROFILE, "")
            dwInterface.sendCommandString(this, DWInterface.DATAWEDGE_SEND_GET_ENUMERATE_SCANNERS, "")
        }
        btn_switch_scanner.setOnClickListener(this)
        btnClearScanHistory.setOnClickListener(this)
    }

    override fun onStart()
    {
        super.onStart()
        //  Ensure the scanner is disabled on the configuration screen, this avoids the user
        //  returning prematurely to the main activity
        dwInterface.sendCommandString(this, DWInterface.DATAWEDGE_SEND_SET_SCANNER_INPUT,
                DWInterface.DATAWEDGE_SEND_SET_SCANNER_INPUT_DISABLE)
    }

    override fun onStop()
    {
        super.onStop()
        switch_ean8.setOnCheckedChangeListener(null)
        switch_ean13.setOnCheckedChangeListener(null)
        switch_code39.setOnCheckedChangeListener(null)
        switch_code128.setOnCheckedChangeListener(null)
        switch_illumination.setOnCheckedChangeListener(null)
        switch_picklist_mode.setOnCheckedChangeListener(null)
        dwInterface.sendCommandString(this, DWInterface.DATAWEDGE_SEND_SET_SCANNER_INPUT, DWInterface.DATAWEDGE_SEND_SET_SCANNER_INPUT_ENABLE)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun update(p0: Observable?, p1: Any?) {
        //  Invoked in response to the DWReceiver broadcast receiver, these are the return values
        //  from DataWedge
        var receivedIntent = p1 as Intent
        if (receivedIntent.hasExtra(DWInterface.DATAWEDGE_EXTRA_RESULT) &&
                receivedIntent.hasExtra(DWInterface.DATAWEDGE_EXTRA_COMMAND))
        {
            //  DataWedge is sending a RESULT from one of the commands.  This app only requests a result
            //  in response to a scanner switch request
            val result = receivedIntent.getStringExtra(DWInterface.DATAWEDGE_EXTRA_RESULT)
            var resultCode = ""
            val resultInfo = receivedIntent.getBundleExtra(DWInterface.DATAWEDGE_EXTRA_RESULT_INFO)
            if (resultInfo.containsKey(DWInterface.DATAWEDGE_EXTRA_RESULT_CODE))
                resultCode = resultInfo.get(DWInterface.DATAWEDGE_EXTRA_RESULT_CODE) as String
            Toast.makeText(this, result + ": " + resultCode, Toast.LENGTH_SHORT).show()
        }
        if (receivedIntent.hasExtra(DWInterface.DATAWEDGE_RETURN_GET_ACTIVE_PROFILE))
        {
            //  DataWedge is sending the Active Profile.  Expect this to match the profile name given
            //  by MainActivity.PROFILE_NAME
            activeProfile = receivedIntent.getStringExtra(DWInterface.DATAWEDGE_RETURN_GET_ACTIVE_PROFILE)
            txtActiveProfile.text = activeProfile
            //  Request the current configuration.  For brevity / readability I have not specified the
            //  bundle keys as constants in DWInterface
            val bMain = Bundle()
            bMain.putString("PROFILE_NAME", activeProfile)
            val bConfig = Bundle()
            val pluginName = ArrayList<String>()
            pluginName.add("BARCODE")
            bConfig.putStringArrayList("PLUGIN_NAME", pluginName)
            bMain.putBundle("PLUGIN_CONFIG", bConfig)
            dwInterface.sendCommandBundle(this, DWInterface.DATAWEDGE_SEND_GET_CONFIG, bMain)
        }
        else if (receivedIntent.hasExtra(DWInterface.DATAWEDGE_RETURN_ENUMERATE_SCANNERS))
        {
            //  DataWedge is sending an array of available scanners on the device, populate the spinner
            try {
                @Suppress("UNCHECKED_CAST")
                val enumeratedScanners =
                        receivedIntent.getSerializableExtra(DWInterface.DATAWEDGE_RETURN_ENUMERATE_SCANNERS) as ArrayList<Bundle>? ?: return
                val scannerList = Array<String>(enumeratedScanners.size) {""}
                for (i in 0 until scannerList.size)
                {
                    val scannerName = enumeratedScanners.get(i).get("SCANNER_NAME") as String
                    scannerList[i] = scannerName
                }
                val adapter = ArrayAdapter<String>(baseContext, android.R.layout.simple_spinner_item, scannerList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner_scanners.setAdapter(adapter)
                spinner_scanners.setSelection(selected_scanner_index)
                spinner_scanners.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        selected_scanner_index = p2
                    }
                }
            }
            catch (e: ClassCastException)
            {
                e.printStackTrace()
                return
            }
        }
        else if (receivedIntent.hasExtra(DWInterface.DATAWEDGE_RETURN_GET_CONFIG))
        {
            //  DataWedge has sent the current configuration, update the UI switches with the current
            //  values for the configurable items.  Again, for readability, I have kept the bundle
            //  keys as Strings rather than constants.
            val configurationBundle = receivedIntent.getBundleExtra(DWInterface.DATAWEDGE_RETURN_GET_CONFIG);
            val pluginConfig = configurationBundle.getParcelableArrayList<Bundle>("PLUGIN_CONFIG") as ArrayList<Bundle>
            val barcodeProps = pluginConfig.get(0).getBundle("PARAM_LIST")!!
            val ean8Enabled = barcodeProps.getString("decoder_ean8")
            if (ean8Enabled != null && ean8Enabled.toLowerCase().equals("true"))
                ean8 = true
            val ean13Enabled = barcodeProps.getString("decoder_ean13")
            if (ean13Enabled != null && ean13Enabled.toLowerCase().equals("true"))
                ean13 = true
            val code39Enabled = barcodeProps.getString("decoder_code39")
            if (code39Enabled != null && code39Enabled.toLowerCase().equals("true"))
                code39 = true
            val code128Enabled = barcodeProps.getString("decoder_code128")
            if (code128Enabled != null && code128Enabled.toLowerCase().equals("true"))
                code128 = true
            val illuminationModeEnabled = barcodeProps.getString("illumination_mode")
            if (illuminationModeEnabled != null && illuminationModeEnabled.toLowerCase().equals("torch"))
            {
                illuminationMode = true
                illuminationModeValue = "torch"
            }
            val picklistModeEnabled = barcodeProps.getString("picklist")
            if (picklistModeEnabled != null && !picklistModeEnabled.toLowerCase().equals("0"))
            {
                picklistMode = true
                picklistModeValue = "2"
            }

            //  Update switches
            switch_ean8.isChecked = ean8
            switch_ean13.isChecked = ean13
            switch_code39.isChecked = code39
            switch_code128.isChecked = code128
            switch_illumination.isChecked = illuminationMode
            switch_picklist_mode.isChecked = picklistMode

            switch_ean8.setOnCheckedChangeListener(this)
            switch_ean13.setOnCheckedChangeListener(this)
            switch_code39.setOnCheckedChangeListener(this)
            switch_code128.setOnCheckedChangeListener(this)
            switch_illumination.setOnCheckedChangeListener(this)
            switch_picklist_mode.setOnCheckedChangeListener(this)
        }
    }

    override fun onCheckedChanged(switch: CompoundButton?, isChecked: Boolean) {
        //  Switch change listeners (for decoders and scanner configuration)
        when (switch?.id)
        {
            R.id.switch_ean8 ->
            {
                ean8 = isChecked
                updateScannerConfig()
            }
            R.id.switch_ean13 ->
            {
                ean13 = isChecked
                updateScannerConfig()
            }
            R.id.switch_code39 ->
            {
                code39 = isChecked
                updateScannerConfig()
            }
            R.id.switch_code128 ->
            {
                code128 = isChecked
                updateScannerConfig()
            }
            R.id.switch_illumination ->
            {
                illuminationMode = isChecked
                if (illuminationMode)
                    illuminationModeValue = "torch"
                else
                    illuminationModeValue = "off"
                updateScannerConfig()
            }
            R.id.switch_picklist_mode ->
            {
                picklistMode = isChecked
                if (picklistMode)
                    picklistModeValue = "2"
                else
                    picklistModeValue = "0"
                updateScannerConfig()
            }
        }
    }

    private fun updateScannerConfig() {
        dwInterface.setConfigForDecoder(this, activeProfile, ean8, ean13, code39, code128,
                illuminationModeValue, picklistModeValue)
        //  It seems whenever we change the scanner configuration it re-enables the scanner plugin
        //  Workaround this by just disabling the scanner input again
        dwInterface.sendCommandString(this, DWInterface.DATAWEDGE_SEND_SET_SCANNER_INPUT, DWInterface.DATAWEDGE_SEND_SET_SCANNER_INPUT_DISABLE)
    }

    override fun onClick(view: View?) {
        //  Button click handlers
        when(view?.id)
        {
            R.id.btn_switch_scanner ->
            {
                dwInterface.sendCommandString(this, DWInterface.DATAWEDGE_SEND_SWITCH_SCANNER,
                        "" + selected_scanner_index, sendResult = true)
                //  It seems whenever we change the scanner configuration it re-enables the scanner plugin
                //  Workaround this by just disabling the scanner input again
                dwInterface.sendCommandString(this, DWInterface.DATAWEDGE_SEND_SET_SCANNER_INPUT, DWInterface.DATAWEDGE_SEND_SET_SCANNER_INPUT_DISABLE)
            }
            R.id.btnClearScanHistory ->
            {
                try {
                    //  Scan history is stored in a file in local storage, just delete that file and the onStart()
                    //  handler of MainActivity will create a new list
                    applicationContext.getFileStreamPath(MainActivity.SCAN_HISTORY_FILE_NAME).delete()
                    Toast.makeText(this, "Data has been cleared", Toast.LENGTH_SHORT).show()
                    this.finish()
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun enableControls()
    {
        //  By default all controls are disabled.  They are only enabled if the DW version is 6.5 or higher
        switch_ean8.isEnabled = true
        switch_ean13.isEnabled = true
        switch_code39.isEnabled = true
        switch_code128.isEnabled = true
        switch_illumination.isEnabled = true
        switch_picklist_mode.isEnabled = true
        spinner_scanners.isEnabled = true
        btn_switch_scanner.isEnabled = true
    }
}
