package net.osdn.ja.gokigen.testsampleapp

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.TextView

class MyActionListener(private val context: Context, private val informationArea: TextView, private val statusArea: TextView) : View.OnClickListener, OnLongClickListener
{
    override fun onClick(p0: View?)
    {
        when (p0?.id) {
            R.id.btnConnect -> pushedConnect()
            R.id.btnWifiSet -> pushedWifiSet()
        }
    }

    override fun onLongClick(p0: View?): Boolean
    {
        return (when (p0?.id)
        {
            R.id.btnConnect -> pushedConnect2()
            R.id.btnWifiSet -> pushedWifiSet2()
            else -> false
        })
    }

    private fun pushedConnect()
    {
        try
        {
            informationArea.text = context.getString(R.string.lbl_connect)
            statusArea.text = ""

        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pushedWifiSet()
    {
        Log.v(TAG, "pushedWifiSet()")
        try
        {
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pushedConnect2() : Boolean
    {
        try
        {
            informationArea.text = ""
            statusArea.text = ""

        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (false)
    }

    private fun pushedWifiSet2() : Boolean
    {
        try
        {
            informationArea.text = ""
            statusArea.text = ""

        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (true)
    }

    companion object
    {
        private val TAG = MyActionListener::class.java.simpleName
    }
}
