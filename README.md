*Please be aware that this application / sample is provided as-is for demonstration purposes without any guarantee of support*
=========================================================

# Integrating DataWedge with your Kotlin application

This application shows how DataWedge can be integrated into your new or existing Kotlin application on Zebra enterprise mobile devices to provide scanning capabilities without having to depend on the native SDK (EMDK). 

![Application](https://raw.githubusercontent.com/darryncampbell/DataWedgeKotlin/master/screenshots/application_01.png)
![Application Configuration](https://raw.githubusercontent.com/darryncampbell/DataWedgeKotlin/master/screenshots/application_02.png)

This application is a "re-imagining" of the official [DataWedge Demo app](http://techdocs.zebra.com/datawedge/latest/guide/demo/) however this app does not come with any kind of warranty or support from Zebra, it is shown for education purposes only.

## DataWedge Intent Interface
DataWedge is a value-add of all Zebra Technologies devices (formally Symbol and Motorola Solutions) that allows barcode capture and configuration without the need to write any code.  This application will demonstrate how to use Android intents to add DataWedge scanning functionality to your application

## Installation / Quick Start
* `git clone https://github.com/darryncampbell/DataWedgeKotlin.git`
* Open in Android Studio and run on any Zebra Android device that supports DataWedge 

## Setup
***Any Zebra mobile computer running Android which supports DataWedge should work with this application but scanner configuration will only be available on devices running DataWedge 6.5 or higher.***

**You will only need to perform the below steps if your DataWedge version is below 6.5**

If your configuration screen has the options grayed out, as shown below:

![Application](https://raw.githubusercontent.com/darryncampbell/DataWedgeKotlin/master/screenshots/application_pre_65.png)

Then you will need to manually configure the DataWedge profile as follows:
* Profile associated with com.darryncampbell.datawedgekotlin
* Barcode input enabled
* Intent output enabled
* Intent action specified to be "com.darryncampbell.datawedgekotlin.SCAN"
* Intent category left on the default value
* Intent delivery specified as "Send via startActivity"
 
The following two screenshots should help you configure DataWedge correctly:

![DW App association](https://raw.githubusercontent.com/darryncampbell/DataWedgeKotlin/master/screenshots/datawedge_02.png)
![DW Intent configuration](https://raw.githubusercontent.com/darryncampbell/DataWedgeKotlin/master/screenshots/datawedge_03.png)

# Use

There are two activities in the application, the main activity demonstrates barcode scanning and initiating the scanner via a soft trigger (button).  The second activity shows how to configure the scanner including enabling / disabling decoders, switching to different scanner hardware or changing some of the scanner properties (specifically, changing the illumination or picklist mode).

Scanning is only available from the MainActivity where you can use either the soft scan trigger or the hardware (yellow) scan buttons on your device.

The history of past scans is stored and will persist across application launches, to clear this history a button is available on the configuration activity.

# Code Examples

*Note that for brevity & readability the capitalized strings represent constants passed to or received from DataWedge.*

Because we have configured DataWedge to invoke startActivity() on each scan and because the application is declared as 'singleTask' in the manifest, we can receive scans via onNewIntent() as follows

``` kotlin
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    //  DataWedge intents received here
    if (intent.hasExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)) {
        //  Handle scan intent received from DataWedge, add it to the list of scans
        var scanData = intent.getStringExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_DATA_STRING)
        var symbology = intent.getStringExtra(DWInterface.DATAWEDGE_SCAN_EXTRA_LABEL_TYPE)
        var date = Calendar.getInstance().getTime()
        var df = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        var dateTimeString = df.format(date)
        var currentScan = Scan(scanData, symbology, dateTimeString);
        scans.add(0, currentScan)
    }
    adapter.notifyDataSetChanged()
}
```

The following code sends a specific command to the scanner (see the [DataWedge API](http://techdocs.zebra.com/datawedge/latest/guide/api/) for a full list of available commands).  

``` kotlin
fun sendCommandString(context: Context, command: String, parameter: String, sendResult: Boolean = false)
{
    val dwIntent = Intent()
    dwIntent.action = DATAWEDGE_SEND_ACTION
    dwIntent.putExtra(command, parameter)
    if (sendResult)
        dwIntent.putExtra(DATAWEDGE_EXTRA_SEND_RESULT, "true")
    context.sendBroadcast(dwIntent)
}
```

And would be called as follows (this example requests the DataWedge version).  

``` kotlin
sendCommandString(this, DWInterface.DATAWEDGE_SEND_GET_VERSION, "")
```

To receive a return value from DataWedge, it is necessary to declare a Broadcast receiver and register with the apprpriate intent filter

``` kotlin
val intentFilter = IntentFilter()
intentFilter.addAction(DWInterface.DATAWEDGE_RETURN_ACTION)
intentFilter.addCategory(DWInterface.DATAWEDGE_RETURN_CATEGORY)
registerReceiver(receiver, intentFilter)
```

In the broadcast receiver you can then take the appropriate action when the DataWedge interface returns a value, in this example, it is returning the DataWedge version

``` kotlin
if (receivedIntent.hasExtra(DWInterface.DATAWEDGE_RETURN_VERSION)) {
    val version = receivedIntent.getBundleExtra(DWInterface.DATAWEDGE_RETURN_VERSION);
    val dataWedgeVersion = version.getString(DWInterface.DATAWEDGE_RETURN_VERSION_DATAWEDGE);
    if (dataWedgeVersion != null && dataWedgeVersion >= "6.5" && !version65OrOver) {
        version65OrOver = true
        createDataWedgeProfile()
    }
}
```
