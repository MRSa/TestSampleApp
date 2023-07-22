package net.osdn.ja.gokigen.testsampleapp

import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MyActionListener(private val activity: AppCompatActivity, private val dataProvider: MyDataProvider, private val informationArea: TextView, private val statusArea: TextView) : View.OnClickListener, OnLongClickListener
{
    override fun onClick(p0: View?)
    {
        when (p0?.id) {
            R.id.btnConnect -> pushedConnect()
            R.id.btnDisconnect -> pushedDisconnect()
            R.id.btnWifiSet -> pushedWifiSet()
        }
    }

    override fun onLongClick(p0: View?): Boolean
    {
        return (when (p0?.id)
        {
            R.id.btnConnect -> pushedConnect2()
            R.id.btnDisconnect -> pushedDisconnect2()
            R.id.btnWifiSet -> pushedWifiSet2()
            else -> false
        })
    }

    private fun pushedConnect()
    {
        try
        {
            val address = dataProvider.getAddress()
            val user = dataProvider.getUser()
            val pass = dataProvider.getPass()
            val message = "${activity.getString(R.string.lbl_connect)} $address  $user"

            informationArea.text = message
            statusArea.text = ""

        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
    private fun pushedDisconnect()
    {
        try
        {
            informationArea.text = activity.getString(R.string.lbl_disconnect)
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
            activity.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
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
    private fun pushedDisconnect2() : Boolean
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
