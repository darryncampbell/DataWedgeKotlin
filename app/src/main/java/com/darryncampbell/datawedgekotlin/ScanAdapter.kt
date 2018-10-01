package com.darryncampbell.datawedgekotlin

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

//  Adapter to map Scan objects to the list in the MainActivity
class ScanAdapter(private var activity: Activity, private var items: ArrayList<Scan>): BaseAdapter()
{
    private class ViewHolder(row: View?)
    {
        var txtScan: TextView? = null;
        var txtSymbology: TextView? = null;
        var txtDateTime: TextView? = null;
        init {
            this.txtScan = row?.findViewById(R.id.scanData)
            this.txtSymbology = row?.findViewById(R.id.symbology)
            this.txtDateTime = row?.findViewById(R.id.dateTime)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View?
        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.scan_item_layout, null)
            viewHolder = ViewHolder(view)
            view?.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        var currentScan = items[position]
        viewHolder.txtScan?.text = currentScan.data
        viewHolder.txtSymbology?.text = currentScan.symbology
        viewHolder.txtDateTime?.text = currentScan.dateTime

        return view as View
    }

    override fun getItem(i: Int): Scan {
        return items[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }
}