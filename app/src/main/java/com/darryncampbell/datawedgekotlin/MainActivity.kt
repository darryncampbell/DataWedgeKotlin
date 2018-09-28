package com.darryncampbell.datawedgekotlin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class MainActivity : AppCompatActivity() {

    private var scans: ArrayList<Scan> = arrayListOf();
    var adapter = ScanAdapter(this, scans)
    private val dwInterface = DataWedgeInterface();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        listView?.adapter = adapter
    }

    override fun onNewIntent(intent: Intent)
    {
        super.onNewIntent(intent)
        //  DataWedge intents received here
        if (intent.hasExtra("com.symbol.datawedge.data_string"))
        {
            //  Handle intent received from DataWedge
            var scanData = intent.getStringExtra("com.symbol.datawedge.data_string");
            var symbology = intent.getStringExtra("com.symbol.datawedge.label_type");
            var date = Calendar.getInstance().getTime()
            var df = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            var dateTimeString = df.format(date)
            var currentScan = Scan(scanData, symbology, dateTimeString);
            scans.add(0, currentScan)
        }
        adapter.notifyDataSetChanged()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings ->
            {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingsIntent);
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
